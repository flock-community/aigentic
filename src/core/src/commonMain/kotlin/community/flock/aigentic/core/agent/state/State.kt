package community.flock.aigentic.core.agent.state

import community.flock.aigentic.core.agent.AgentRun
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.status.AgentStatus
import community.flock.aigentic.core.agent.status.toStatus
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
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
    internal var systemPromptMessage: Message.SystemPrompt? = null,
    internal val messages: MutableSharedFlow<Message> = MutableSharedFlow(replay = 1000),
) {
    val messagesAccessor = Messages(state = this)
}

class Messages(
    private val state: State,
) {
    private fun categoryOrder(category: MessageCategory): Int =
        when (category) {
            MessageCategory.SYSTEM_PROMPT -> 0
            MessageCategory.CONFIG_CONTEXT -> 1
            MessageCategory.EXAMPLE -> 2
            MessageCategory.RUN_CONTEXT -> 3
            MessageCategory.EXECUTION -> 4
        }

    fun snapshot(): List<Message> =
        (listOfNotNull(state.systemPromptMessage) + state.messages.replayCache)
            .sortedBy { categoryOrder(it.category) }

    fun asFlow() = state.messages
}

@PublishedApi
internal fun State.getStatus() = merge(messagesAccessor.asFlow().flatMapConcat { it.toStatus().asFlow() }, events)

@PublishedApi
internal suspend fun State.addRunExecutionMessage(message: Message) = this.messages.emit(message)

suspend fun State.addConfigContextMessage(message: Message) = this.messages.emit(message)

@PublishedApi
internal suspend fun State.addRunContextMessage(message: Message) = this.messages.emit(message)

@PublishedApi
internal suspend fun State.addExampleMessage(message: Message) = this.messages.emit(message)

fun State.addSystemPromptMessage(message: Message.SystemPrompt) {
    this.systemPromptMessage = message
}

@PublishedApi
internal suspend fun State.addModelRequestInfo(modelRequestInfo: ModelRequestInfo) = this.modelRequestInfos.emit(modelRequestInfo)

@PublishedApi
internal suspend fun State.addExampleRunId(run: RunId) = this.exampleRunIds.emit(run)

@PublishedApi
internal fun <O : Any> Pair<State, Outcome<O>>.toRun(): AgentRun<O> =
    with(first) {
        val systemPromptMessage = this.systemPromptMessage ?: aigenticException("System prompt message must be present in agent run")

        AgentRun(
            startedAt = startedAt,
            finishedAt = finishedAt ?: Clock.System.now(),
            messages = messagesAccessor.snapshot().filterNot { it is Message.ExampleToolMessage },
            outcome = second,
            modelRequests = modelRequestInfos.replayCache,
            exampleRunIds = exampleRunIds.replayCache,
            systemPromptMessage = systemPromptMessage,
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
