package community.flock.aigentic.core.agent.state

import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.status.AgentStatus
import community.flock.aigentic.core.agent.status.toStatus
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.merge
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class State(
    val startedAt: Instant = Clock.System.now(),
    var finishedAt: Instant? = null,
    val messages: MutableSharedFlow<Message> = MutableSharedFlow(replay = 1000),
    val events: MutableSharedFlow<AgentStatus> = MutableSharedFlow(replay = 1000),
    val modelRequestInfos: MutableSharedFlow<ModelRequestInfo> = MutableSharedFlow(replay = 1000),
    val exampleRunIds: MutableSharedFlow<RunId> = MutableSharedFlow(replay = 1000),
)

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
internal fun <O : Any> Pair<State, Result<O>>.toRun(): Run<O> =
    with(first) {
        Run(
            startedAt = startedAt,
            finishedAt = finishedAt ?: Clock.System.now(),
            messages = messages.replayCache.filter { message -> message.messageType is MessageType.New },
            result = second,
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
