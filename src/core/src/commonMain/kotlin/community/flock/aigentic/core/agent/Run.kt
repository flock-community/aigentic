package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.message.Message
import kotlinx.serialization.json.Json
import kotlin.Deprecated
import kotlin.ReplaceWith
import kotlin.jvm.JvmInline
import kotlin.time.Instant

sealed class Run<O : Any> {
    abstract val startedAt: Instant
    abstract val finishedAt: Instant
    abstract val messages: List<Message>
    abstract val outcome: Outcome<O>
    abstract val modelRequests: List<ModelRequestInfo>
}

data class AgentRun<O : Any>(
    override val startedAt: Instant,
    override val finishedAt: Instant,
    override val messages: List<Message>,
    override val outcome: Outcome<O>,
    override val modelRequests: List<ModelRequestInfo>,
    val systemPromptMessage: Message.SystemPrompt,
    val exampleRunIds: List<RunId> = emptyList(),
    val configContextMessages: List<Message> = emptyList(),
    val runAttachmentMessages: List<Message> = emptyList(),
    val executionMessages: List<Message> = emptyList(),
) : Run<O>()

data class WorkflowRun<O : Any>(
    override val startedAt: Instant,
    override val finishedAt: Instant,
    override val messages: List<Message>,
    override val outcome: Outcome<O>,
    override val modelRequests: List<ModelRequestInfo>,
    val agentRuns: List<AgentRun<*>>,
) : Run<O>()

@JvmInline
value class RunTag(val value: String)

@JvmInline
value class RunId(val value: String)

data class TokenUsage(
    val inputTokens: Int,
    val outputTokens: Int,
    val thinkingOutputTokens: Int,
    val cachedInputTokens: Int,
)

@PublishedApi
internal fun <O : Any> createWorkflowRun(
    agentRuns: List<AgentRun<*>>,
    finalOutcome: Outcome<O>,
): WorkflowRun<O> {
    require(agentRuns.isNotEmpty()) { "Workflow must have at least one agent run" }

    val firstRun = agentRuns.first()
    val lastRun = agentRuns.last()

    return WorkflowRun(
        startedAt = firstRun.startedAt,
        finishedAt = lastRun.finishedAt,
        messages = agentRuns.flatMap { it.messages },
        outcome = finalOutcome,
        modelRequests = agentRuns.flatMap { it.modelRequests },
        agentRuns = agentRuns,
    )
}

fun <O : Any> Run<O>.tokenUsage(): TokenUsage =
    TokenUsage(
        inputTokens = modelRequests.sumOf { it.inputTokenCount },
        outputTokens = modelRequests.sumOf { it.outputTokenCount },
        thinkingOutputTokens = modelRequests.sumOf { it.thinkingOutputTokenCount },
        cachedInputTokens = modelRequests.sumOf { it.cachedInputTokenCount },
    )

@Deprecated("Use tokenUsage().inputTokens instead", ReplaceWith("tokenUsage().inputTokens"))
fun <O : Any> Run<O>.inputTokens(): Int = modelRequests.sumOf { it.inputTokenCount }

@Deprecated("Use tokenUsage().outputTokens instead", ReplaceWith("tokenUsage().outputTokens"))
fun <O : Any> Run<O>.outputTokens(): Int = modelRequests.sumOf { it.outputTokenCount }

@Deprecated("Use tokenUsage().thinkingOutputTokens instead", ReplaceWith("tokenUsage().thinkingOutputTokens"))
fun <O : Any> Run<O>.thinkingOutputTokens(): Int = modelRequests.sumOf { it.thinkingOutputTokenCount }

@Deprecated("Use tokenUsage().cachedInputTokens instead", ReplaceWith("tokenUsage().cachedInputTokens"))
fun <O : Any> Run<O>.cachedInputTokens(): Int = modelRequests.sumOf { it.cachedInputTokenCount }

@PublishedApi
internal inline fun <reified O : Any> AgentRun<String>.decode(): AgentRun<O> {
    val currentOutcome = outcome
    return AgentRun(
        startedAt = startedAt,
        finishedAt = finishedAt,
        messages = messages,
        outcome =
            when (currentOutcome) {
                is Outcome.Fatal -> currentOutcome
                is Outcome.Finished<String> ->
                    Outcome.Finished(
                        description = currentOutcome.description,
                        response = currentOutcome.response?.let { Json.decodeFromString<O>(it) },
                    )

                is Outcome.Stuck -> currentOutcome
            },
        modelRequests = modelRequests,
        exampleRunIds = exampleRunIds,
        systemPromptMessage = systemPromptMessage,
        configContextMessages = configContextMessages,
        runAttachmentMessages = runAttachmentMessages,
        executionMessages = executionMessages,
    )
}
