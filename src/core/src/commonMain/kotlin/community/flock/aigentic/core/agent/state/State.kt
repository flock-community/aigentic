package community.flock.aigentic.core.agent.state

import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.status.toStatus
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.message.Message
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class State(
    val startedAt: Instant = Clock.System.now(),
    var finishedAt: Instant? = null,
    val messages: MutableSharedFlow<Message> = MutableSharedFlow(replay = 100),
)

fun State.getMessages() = messages.asSharedFlow()

fun State.getStatus() = messages.flatMapConcat { it.toStatus().asFlow() }

internal suspend fun State.addMessages(messages: List<Message>) = messages.forEach { addMessage(it) }

internal suspend fun State.addMessage(message: Message) = this.messages.emit(message)

fun Pair<State, Result>.toRun(): Run =
    with(first) {
        Run(
            startedAt = startedAt,
            finishedAt = finishedAt ?: Clock.System.now(),
            messages = messages.replayCache,
            result = second,
        )
    }
