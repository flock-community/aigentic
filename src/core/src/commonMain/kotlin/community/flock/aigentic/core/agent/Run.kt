package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.message.Message
import kotlinx.datetime.Instant
import kotlin.jvm.JvmInline

data class Run(
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

fun Run.inputTokens(): Int = modelRequests.sumOf { it.inputTokenCount }

fun Run.outputTokens(): Int = modelRequests.sumOf { it.outputTokenCount }

fun Run.thinkingOutputTokens(): Int = modelRequests.sumOf { it.thinkingOutputTokenCount }

fun Run.cachedInputTokens(): Int = modelRequests.sumOf { it.cachedInputTokenCount }
