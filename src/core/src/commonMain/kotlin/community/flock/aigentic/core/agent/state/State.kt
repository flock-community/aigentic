package community.flock.aigentic.core.agent.state

import community.flock.aigentic.core.agent.AgentRun
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.status.AgentStatus
import community.flock.aigentic.core.agent.status.toStatus
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.message.Message
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.merge
import kotlin.time.Clock
import kotlin.time.Instant

data class State(
    val startedAt: Instant = Clock.System.now(),
    var finishedAt: Instant? = null,
    val events: MutableSharedFlow<AgentStatus> = MutableSharedFlow(replay = 1000),
    val modelRequestInfos: MutableSharedFlow<ModelRequestInfo> = MutableSharedFlow(replay = 1000),
    val exampleRunIds: MutableSharedFlow<RunId> = MutableSharedFlow(replay = 1000),
    internal val systemPromptMessages: MutableSharedFlow<Message> = MutableSharedFlow(replay = 1),
    internal val configContextMessages: MutableSharedFlow<Message> = MutableSharedFlow(replay = 1000),
    internal val runContextMessages: MutableSharedFlow<Message> = MutableSharedFlow(replay = 1000),
    internal val exampleMessages: MutableSharedFlow<Message> = MutableSharedFlow(replay = 1000),
    internal val runExecutionMessages: MutableSharedFlow<Message> = MutableSharedFlow(replay = 1000),
) {
    val messages =
        Messages(
            systemPromptMessages = systemPromptMessages,
            configContextMessages = configContextMessages,
            runContextMessages = runContextMessages,
            exampleMessages = exampleMessages,
            runExecutionMessages = runExecutionMessages,
        )

    companion object {
        @PublishedApi
        internal fun <O : Any> fromRun(run: AgentRun<O>): State =
            State(
                startedAt = run.startedAt,
                finishedAt = run.finishedAt,
            ).apply {
                run.configContextMessages.forEach(configContextMessages::tryEmit)
                run.runAttachmentMessages.forEach(runContextMessages::tryEmit)
                run.messages.forEach(runExecutionMessages::tryEmit)
                run.modelRequests.forEach(modelRequestInfos::tryEmit)
                run.exampleRunIds.forEach(exampleRunIds::tryEmit)
            }
    }
}

class Messages(
    private val systemPromptMessages: MutableSharedFlow<Message>,
    private val configContextMessages: MutableSharedFlow<Message>,
    private val runContextMessages: MutableSharedFlow<Message>,
    private val exampleMessages: MutableSharedFlow<Message>,
    private val runExecutionMessages: MutableSharedFlow<Message>,
) {
    fun snapshot(): List<Message> =
        systemPromptMessages.replayCache +
            configContextMessages.replayCache +
            runContextMessages.replayCache +
            exampleMessages.replayCache +
            runExecutionMessages.replayCache

    fun snapshotForRun(): List<Message> =
        systemPromptMessages.replayCache +
            configContextMessages.replayCache +
            runContextMessages.replayCache +
            runExecutionMessages.replayCache

    fun asFlow() =
        merge(
            systemPromptMessages,
            configContextMessages,
            runContextMessages,
            exampleMessages,
            runExecutionMessages,
        )
}

@PublishedApi
internal fun State.getStatus() = merge(messages.asFlow().flatMapConcat { it.toStatus().asFlow() }, events)

@PublishedApi
internal suspend fun State.addMessages(messages: List<Message>) = messages.forEach { addRunExecutionMessage(it) }

@PublishedApi
internal suspend fun State.addRunExecutionMessage(message: Message) = this.runExecutionMessages.emit(message)

suspend fun State.addConfigContextMessage(message: Message) = this.configContextMessages.emit(message)

@PublishedApi
internal suspend fun State.addRunContextMessage(message: Message) = this.runContextMessages.emit(message)

@PublishedApi
internal suspend fun State.addExampleMessage(message: Message) = this.exampleMessages.emit(message)

suspend fun State.addSystemPromptMessage(message: Message.SystemPrompt) = this.systemPromptMessages.emit(message)

@PublishedApi
internal suspend fun State.addModelRequestInfo(modelRequestInfo: ModelRequestInfo) = this.modelRequestInfos.emit(modelRequestInfo)

@PublishedApi
internal suspend fun State.addExampleRunId(run: RunId) = this.exampleRunIds.emit(run)

@PublishedApi
internal fun <O : Any> Pair<State, Outcome<O>>.toRun(): AgentRun<O> =
    with(first) {
        AgentRun(
            startedAt = startedAt,
            finishedAt = finishedAt ?: Clock.System.now(),
            messages = messages.snapshotForRun(),
            outcome = second,
            modelRequests = modelRequestInfos.replayCache,
            exampleRunIds = exampleRunIds.replayCache,
            configContextMessages = configContextMessages.replayCache,
            runAttachmentMessages = runContextMessages.replayCache,
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
