package community.flock.aigentic.koog

import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentStorageKey
import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.feature.AIAgentGraphFeature
import ai.koog.agents.core.feature.pipeline.AIAgentGraphPipeline
import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.AgentRun
import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.platform.RunSentResult
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.koog.mapper.systemPromptText
import community.flock.aigentic.koog.mapper.toAigenticMessages
import community.flock.aigentic.koog.mapper.toAigenticTool
import community.flock.aigentic.koog.mapper.toAigenticToolResultMessage
import community.flock.aigentic.koog.model.KoogModel
import community.flock.aigentic.koog.model.KoogModelIdentifier
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Reports Koog agent runs to the Aigentic platform. Install like any other Koog feature:
 *
 * ```
 * AIAgent(promptExecutor = ..., llmModel = ..., systemPrompt = "...") {
 *     install(Aigentic) {
 *         task = Task("Answer questions", emptyList())
 *     }
 * }
 * ```
 *
 * `platform` defaults from `AIGENTIC_PLATFORM_NAME`/`_SECRET`/`_URL` (see [defaultAigenticPlatform]).
 * See [AigenticConfig] for the rest of the configuration surface (structured output, tags,
 * example-run seeding).
 */
class Aigentic {
    companion object Feature : AIAgentGraphFeature<AigenticConfig, Aigentic> {
        override val key: AIAgentStorageKey<Aigentic> = createStorageKey("aigentic-reporting")

        override fun createInitialConfig(agentConfig: AIAgentConfig): AigenticConfig = AigenticConfig()

        override fun install(
            config: AigenticConfig,
            pipeline: AIAgentGraphPipeline,
        ): Aigentic {
            var startedAt: Instant? = null
            var systemPromptMessage: Message.SystemPrompt? = null
            var koogModel: KoogModel? = null
            var tools: Map<ToolName, Tool> = emptyMap()
            var lastLLMCallStartedAt: Instant? = null
            val messages = mutableListOf<Message>()
            val modelRequests = mutableListOf<ModelRequestInfo>()

            val report: suspend (Outcome<Any>) -> Unit = { outcome ->
                val agent =
                    Agent<String, Any>(
                        platform = null,
                        customSystemPromptBuilder = null,
                        model = koogModel ?: KoogModel(KoogModelIdentifier("unknown")),
                        task = config.task,
                        contexts = emptyList(),
                        tools = tools,
                        tags = config.tags,
                    )
                val prompt = systemPromptMessage ?: Message.SystemPrompt(prompt = "")
                val run =
                    AgentRun(
                        startedAt = startedAt ?: Clock.System.now(),
                        finishedAt = Clock.System.now(),
                        messages = listOf(prompt) + messages,
                        outcome = outcome,
                        modelRequests = modelRequests.toList(),
                        systemPromptMessage = prompt,
                        exampleRunIds = config.exampleRunIds,
                    )
                val result = config.platform.client.sendRun(run, agent, config.outputSerializer, null)
                if (result is RunSentResult.Success) config.onRunReported(result.runId)
            }

            pipeline.interceptAgentStarting(this) { ctx ->
                startedAt = Clock.System.now()
                // ctx.context.agentInput is the raw run() input, captured before the strategy graph
                // appends it (or any fetchExampleRunPrompt-spliced example messages) to the live prompt -
                // reading it back off the live prompt in onLLMCallStarting would otherwise pick up
                // whichever user-role message happens to come first, which is the spliced example
                // preamble when one is present, not the real question.
                ctx.context.agentInput?.toString()?.let {
                    messages += Message.Text(sender = Sender.Agent, text = it, category = MessageCategory.RUN_CONTEXT)
                }
            }

            pipeline.interceptLLMCallStarting(this) { ctx ->
                lastLLMCallStartedAt = Clock.System.now()
                if (systemPromptMessage == null) {
                    systemPromptMessage = Message.SystemPrompt(prompt = ctx.prompt.systemPromptText())
                    koogModel = KoogModel(KoogModelIdentifier(ctx.model.id))
                    tools = ctx.tools.associate { ToolName(it.name) to it.toAigenticTool() }
                }
            }

            pipeline.interceptLLMCallCompleted(this) { ctx ->
                val finishedAt = Clock.System.now()
                ctx.response?.let { messages += it.toAigenticMessages() }
                modelRequests +=
                    ModelRequestInfo(
                        startedAt = lastLLMCallStartedAt ?: finishedAt,
                        finishedAt = finishedAt,
                        inputTokenCount = ctx.response?.metaInfo?.inputTokensCount ?: 0,
                        outputTokenCount = ctx.response?.metaInfo?.outputTokensCount ?: 0,
                    )
            }

            pipeline.interceptToolCallCompleted(this) { ctx -> messages += ctx.toAigenticToolResultMessage() }

            pipeline.interceptAgentCompleted(this) { ctx ->
                report(Outcome.Finished(description = "Koog agent finished", response = ctx.result))
            }

            pipeline.interceptAgentExecutionFailed(this) { ctx ->
                report(Outcome.Fatal(message = ctx.error.message ?: "Koog agent execution failed"))
            }

            return Aigentic()
        }
    }
}
