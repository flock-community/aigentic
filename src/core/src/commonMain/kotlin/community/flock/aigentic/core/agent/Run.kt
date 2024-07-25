package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.message.Message
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

data class Run(
    val startedAt: Instant,
    val finishedAt: Instant,
    val messages: List<Message>,
    val result: Result,
    val modelRequests: List<ModelRequestInfo>,
)

fun Run.inputTokens(): Int = modelRequests.sumOf { it.inputTokenCount }

fun Run.outputTokens(): Int = modelRequests.sumOf { it.outputTokenCount }

inline fun <reified T> Result.Finished.getFinishResponse(): T? = response?.let { Json.decodeFromString(it) }
