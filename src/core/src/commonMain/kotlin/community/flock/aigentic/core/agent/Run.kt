package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.message.Message
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmInline

data class Run<O : Any>(
    val startedAt: Instant,
    val finishedAt: Instant,
    val messages: List<Message>,
    val result: Result<O>,
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

inline fun <reified O : Any> Run<String>.decode(): Run<O> {
    return Run(
        startedAt = startedAt,
        finishedAt = finishedAt,
        messages = messages,
        result =
            when (result) {
                is Result.Fatal -> result
                is Result.Finished<String> ->
                    Result.Finished(
                        description = result.description,
                        response = result.response?.let { Json.decodeFromString<O>(it) },
                    )

                is Result.Stuck -> result
            },
        modelRequests = modelRequests,
        exampleRunIds = exampleRunIds,
    )
}

inline fun <reified O : Any> Run<O>.finishResponse(): O? =
    when (result) {
        is Result.Finished -> result.response
        is Result.Fatal -> error("Cannot read finsh response from: Fatal")
        is Result.Stuck -> error("Cannot read finsh response from: Stuck")
    }
