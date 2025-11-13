package community.flock.aigentic.core.agent.state

import community.flock.aigentic.core.agent.AgentRun
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.status.AgentStatus
import community.flock.aigentic.core.agent.status.toStatus
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.merge
import kotlin.time.Clock
import kotlin.time.Instant

data class State(
    val startedAt: Instant = Clock.System.now(),
    var finishedAt: Instant? = null,
    val messages: MutableSharedFlow<Message> = MutableSharedFlow(replay = 1000),
    val events: MutableSharedFlow<AgentStatus> = MutableSharedFlow(replay = 1000),
    val modelRequestInfos: MutableSharedFlow<ModelRequestInfo> = MutableSharedFlow(replay = 1000),
    val exampleRunIds: MutableSharedFlow<RunId> = MutableSharedFlow(replay = 1000),
) {
    companion object {
        @PublishedApi
        internal fun <O : Any> fromRun(run: AgentRun<O>): State =
            State(
                startedAt = run.startedAt,
                finishedAt = run.finishedAt,
            ).apply {
                run.messages.forEach(messages::tryEmit)
                run.modelRequests.forEach(modelRequestInfos::tryEmit)
                run.exampleRunIds.forEach(exampleRunIds::tryEmit)
            }
    }
}

internal fun State.getMessages() = messages.asSharedFlow()

@PublishedApi
internal fun State.getStatus() = merge(messages.flatMapConcat { it.toStatus().asFlow() }, events)

@PublishedApi
internal suspend fun State.addMessages(messages: List<Message>) = messages.forEach { addMessage(it) }

@PublishedApi
internal suspend fun State.addMessage(message: Message) = this.messages.emit(message)

@PublishedApi
internal suspend fun State.addModelRequestInfo(modelRequestInfo: ModelRequestInfo) = this.modelRequestInfos.emit(modelRequestInfo)

@PublishedApi
internal suspend fun State.addExampleRun(run: RunId) = this.exampleRunIds.emit(run)

@PublishedApi
internal fun <O : Any> Pair<State, Outcome<O>>.toRun(): AgentRun<O> =
    with(first) {
        AgentRun(
            startedAt = startedAt,
            finishedAt = finishedAt ?: Clock.System.now(),
            messages = messages.replayCache.filter { message -> message.category != MessageCategory.EXAMPLE },
            outcome = second,
            modelRequests = modelRequestInfos.replayCache,
            exampleRunIds = exampleRunIds.replayCache,
        )
    }

data class ModelRequestInfo(
    val startedAt: Instant,
    val finishedAt: Instant,
    val inputTokenCount: Int,
    val outputTokenCount: Int,
    val thinkingOutputTokenCount: Int = 0,
    val cachedInputTokenCount: Int = 0,
)
