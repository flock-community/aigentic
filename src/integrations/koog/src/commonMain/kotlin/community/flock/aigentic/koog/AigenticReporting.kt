package community.flock.aigentic.koog

import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.features.eventHandler.feature.EventHandler
import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.AgentRun
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.agent.Task
import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.platform.RunSentResult
import community.flock.aigentic.core.platform.sendRun
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.koog.mapper.initialUserText
import community.flock.aigentic.koog.mapper.systemPromptText
import community.flock.aigentic.koog.mapper.toAigenticMessages
import community.flock.aigentic.koog.mapper.toAigenticTool
import community.flock.aigentic.koog.mapper.toAigenticToolResultMessage
import community.flock.aigentic.koog.model.KoogModel
import community.flock.aigentic.koog.model.KoogModelIdentifier
import kotlin.time.Clock
import kotlin.time.Instant

inline fun <reified Output : Any> GraphAIAgent.FeatureContext.reportRunsToAigentic(
    platform: Platform,
    task: Task,
    tags: List<RunTag> = emptyList(),
    noinline onRunReported: (RunId) -> Unit = {},
) {
    install(EventHandler) {
        var startedAt: Instant? = null
        var systemPromptMessage: Message.SystemPrompt? = null
        var koogModel: KoogModel? = null
        var tools: Map<ToolName, Tool> = emptyMap()
        var lastLLMCallStartedAt: Instant? = null
        val messages = mutableListOf<Message>()
        val modelRequests = mutableListOf<ModelRequestInfo>()

        val report: suspend (Outcome<Output>) -> Unit = { outcome ->
            val agent =
                Agent<String, Output>(
                    platform = null,
                    customSystemPromptBuilder = null,
                    model = koogModel ?: KoogModel(KoogModelIdentifier("unknown", "unknown")),
                    task = task,
                    contexts = emptyList(),
                    tools = tools,
                    tags = tags,
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
                )
            val result = platform.sendRun(run, agent)
            if (result is RunSentResult.Success) onRunReported(result.runId)
        }

        onAgentStarting { startedAt = Clock.System.now() }

        onLLMCallStarting { ctx ->
            lastLLMCallStartedAt = Clock.System.now()
            if (systemPromptMessage == null) {
                systemPromptMessage = Message.SystemPrompt(prompt = ctx.prompt.systemPromptText())
                ctx.prompt.initialUserText()?.let {
                    messages += Message.Text(sender = Sender.Agent, text = it, category = MessageCategory.RUN_CONTEXT)
                }
                koogModel = KoogModel(KoogModelIdentifier(ctx.model.provider.id, ctx.model.id))
                tools = ctx.tools.associate { ToolName(it.name) to it.toAigenticTool() }
            }
        }

        onLLMCallCompleted { ctx ->
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

        onToolCallCompleted { ctx -> messages += ctx.toAigenticToolResultMessage() }

        onAgentCompleted { ctx ->
            report(Outcome.Finished(description = "Koog agent finished", response = ctx.result as? Output))
        }

        onAgentExecutionFailed { ctx ->
            report(Outcome.Fatal(message = ctx.error.message ?: "Koog agent execution failed"))
        }

    }
}
