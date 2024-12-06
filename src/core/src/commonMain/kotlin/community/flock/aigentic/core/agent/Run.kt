package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.message.Message
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmInline

data class Run(
    val startedAt: Instant,
    val finishedAt: Instant,
    val messages: List<Message>,
    val result: Result,
    val modelRequests: List<ModelRequestInfo>,
    val linkedRuns: List<RunId>? = listOf(),
)

@JvmInline
value class RunTag(val value: String)

@JvmInline
value class RunId(val value: String)

fun Run.inputTokens(): Int = modelRequests.sumOf { it.inputTokenCount }

fun Run.outputTokens(): Int = modelRequests.sumOf { it.outputTokenCount }

inline fun <reified T> Result.Finished.getFinishResponse(): T? = response?.let { Json.decodeFromString(it) }
