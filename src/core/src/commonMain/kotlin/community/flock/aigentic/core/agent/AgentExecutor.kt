package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.Action.ExecuteTools
import community.flock.aigentic.core.agent.Action.Finished
import community.flock.aigentic.core.agent.Action.Initialize
import community.flock.aigentic.core.agent.Action.ProcessModelResponse
import community.flock.aigentic.core.agent.Action.SendModelRequest
import community.flock.aigentic.core.agent.message.correctionMessage
import community.flock.aigentic.core.agent.state.State
import community.flock.aigentic.core.agent.state.addMessage
import community.flock.aigentic.core.agent.state.addMessages
import community.flock.aigentic.core.agent.state.getStatus
import community.flock.aigentic.core.agent.state.toRun
import community.flock.aigentic.core.agent.tool.FinishedOrStuck
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Sender.Aigentic
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.model.ModelResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map

suspend fun Agent.start(): Run =
    coroutineScope {
        val state = State()
        val logging = async { state.getStatus().map { it.text }.collect(::println) }

        executeAction(Initialize(state, this@start)).also {
            delay(10) // Allow some time for the logging to finish
            logging.cancelAndJoin()
        }.toRun()
    }

private suspend fun executeAction(action: Action): Pair<State, FinishedOrStuck> =
    when (action) {
        is Initialize -> executeAction(action.process(action.state))
        is SendModelRequest -> executeAction(action.process(action.state))
        is ProcessModelResponse -> executeAction(action.process(action.state))
        is ExecuteTools -> executeAction(action.process(action.state))
        is Finished -> action.process(action.state)
    }

private suspend fun Initialize.process(state: State): Action {
    state.addMessages(initializeStartMessages(agent))
    return SendModelRequest(state, agent)
}

private suspend fun ProcessModelResponse.process(state: State): Action =
    when (responseMessage) {
        is Message.ToolCalls -> ExecuteTools(state, agent, responseMessage.toolCalls)
        else -> {
            state.messages.emit(correctionMessage)
            SendModelRequest(state, agent)
        }
    }

private suspend fun SendModelRequest.process(state: State): ProcessModelResponse {
    val message = agent.sendModelRequest(state).message
    state.addMessage(message)
    return ProcessModelResponse(state, agent, message)
}

private fun Finished.process(state: State) = state to finishedOrStuck

private suspend fun ExecuteTools.process(state: State): Action {
    val toolResults = executeToolCalls(agent, toolCalls)
    val finished = toolResults.filterIsInstance<ToolExecutionResult.FinishedToolResult>().firstOrNull()

    return if (finished != null) {
        Finished(state, agent, finished.reason)
    } else {
        state.addMessages(toolResults.filterIsInstance<ToolExecutionResult.ToolResult>().map { it.message })
        SendModelRequest(state, agent)
    }
}

private fun initializeStartMessages(agent: Agent): List<Message> =
    listOf(
        agent.systemPromptBuilder.buildSystemPrompt(agent),
    ) +
        agent.contexts.map {
            when (it) {
                is Context.Image -> Message.Image(Aigentic, it.base64)
                is Context.Text -> Message.Text(Aigentic, it.text)
            }
        }


private suspend fun Agent.sendModelRequest(state: State): ModelResponse =
    model.sendRequest(state.messages.replayCache, tools.values.toList() + internalTools.values.toList())

private sealed interface Action {
    data class Initialize(val state: State, val agent: Agent) : Action

    data class ExecuteTools(val state: State, val agent: Agent, val toolCalls: List<ToolCall>) : Action

    data class SendModelRequest(val state: State, val agent: Agent) : Action

    data class ProcessModelResponse(val state: State, val agent: Agent, val responseMessage: Message) : Action

    data class Finished(val state: State, val agent: Agent, val finishedOrStuck: FinishedOrStuck) : Action
}

