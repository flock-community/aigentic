package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.message.Message
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
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

fun <O : Any> Run<O>.inputTokens(): Int = modelRequests.sumOf { it.inputTokenCount }

fun <O : Any> Run<O>.outputTokens(): Int = modelRequests.sumOf { it.outputTokenCount }

fun <O : Any> Run<O>.thinkingOutputTokens(): Int = modelRequests.sumOf { it.thinkingOutputTokenCount }

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
