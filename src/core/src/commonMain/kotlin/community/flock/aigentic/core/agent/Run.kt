package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.message.Message
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.Deprecated
import kotlin.ReplaceWith
import kotlin.jvm.JvmInline

data class Run<O : Any>(
    val startedAt: Instant,
    val finishedAt: Instant,
    val messages: List<Message>,
    val outcome: Outcome<O>,
    val modelRequests: List<ModelRequestInfo>,
    val exampleRunIds: List<RunId> = emptyList(),
)

@JvmInline
value class RunTag(val value: String)

@JvmInline
value class RunId(val value: String)

data class TokenUsage(
    val inputTokens: Int,
    val outputTokens: Int,
    val thinkingOutputTokens: Int,
    val cachedInputTokens: Int,
) {
    val totalTokens: Int get() = inputTokens + outputTokens + thinkingOutputTokens
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
internal inline fun <reified O : Any> Run<String>.decode(): Run<O> {
    return Run(
        startedAt = startedAt,
        finishedAt = finishedAt,
        messages = messages,
        outcome =
            when (outcome) {
                is Outcome.Fatal -> outcome
                is Outcome.Finished<String> ->
                    Outcome.Finished(
                        description = outcome.description,
                        response = outcome.response?.let { Json.decodeFromString<O>(it) },
                    )

                is Outcome.Stuck -> outcome
            },
        modelRequests = modelRequests,
        exampleRunIds = exampleRunIds,
    )
}
