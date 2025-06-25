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
    val result: Result,
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

inline fun <reified O : Any> Run<O>.finishResponse(): O? = when (result) {
    is Result.Finished -> result.response?.let { Json.decodeFromString(it) }
    is Result.Fatal -> error("Cannot read finsh response from: Fatal")
    is Result.Stuck -> error("Cannot read finsh response from: Stuck")
}
