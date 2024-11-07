package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.Action.ExecuteTools
import community.flock.aigentic.core.agent.Action.Finished
import community.flock.aigentic.core.agent.Action.Initialize
import community.flock.aigentic.core.agent.Action.ProcessModelResponse
import community.flock.aigentic.core.agent.Action.SendModelRequest
import community.flock.aigentic.core.agent.message.correctionMessage
import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.state.State
import community.flock.aigentic.core.agent.state.addMessage
import community.flock.aigentic.core.agent.state.addMessages
import community.flock.aigentic.core.agent.state.addModelRequestInfo
import community.flock.aigentic.core.agent.state.getStatus
import community.flock.aigentic.core.agent.state.toRun
import community.flock.aigentic.core.agent.status.AgentStatus
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.exception.AigenticException
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.platform.RunSentResult
import community.flock.aigentic.core.util.withStartFinishTiming
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map

suspend fun Agent.start(): Run =
    coroutineScope {
        val state = State()
        state.events.emit(AgentStatus.Started)
        val logging = async { state.getStatus().map { it.text }.collect(::println) }
        try {
            val run = executeAction(Initialize(state, this@start)).toRun()
            publishRun(this@start, run, state)
            run
        } catch (e: AigenticException) {
            state.events.emit(AgentStatus.Fatal(e.message))
            val run = (state to Result.Fatal(e.message)).toRun()
            publishRun(this@start, run, state)
            run
        } finally {
            delay(10) // Allow some time for the logging to finish
            logging.cancelAndJoin()
        }
    }

private suspend fun publishRun(
    agent: Agent,
    run: Run,
    state: State,
) {
    if (agent.platform != null) {
        when (val result = agent.platform.sendRun(run, agent)) {
            RunSentResult.Success -> state.events.emit(AgentStatus.PublishedRunSuccess)
            RunSentResult.Unauthorized -> state.events.emit(AgentStatus.PublishedRunUnauthorized)
            is RunSentResult.Error -> state.events.emit(AgentStatus.PublishedRunError(result.message))
        }
    }
}

suspend fun executeAction(action: Action): Pair<State, Result> =
    when (action) {
        is Initialize -> executeAction(action.process())
        is SendModelRequest -> executeAction(action.process())
        is ProcessModelResponse -> executeAction(action.process())
        is ExecuteTools -> executeAction(action.process())
        is Finished -> action.process()
    }

private suspend fun Initialize.process(): Action {
    state.addMessages(initializeStartMessages(agent))
    return SendModelRequest(state, agent)
}

private suspend fun ProcessModelResponse.process(): Action =
    when (responseMessage) {
        is Message.ToolCalls -> ExecuteTools(state, agent, responseMessage.toolCalls)
        else -> {
            state.messages.emit(correctionMessage)
            SendModelRequest(state, agent)
        }
    }

private suspend fun SendModelRequest.process(): ProcessModelResponse {
    val (startedAt, finishedAt, response) =
        withStartFinishTiming {
            agent.sendModelRequest(state)
        }

    state.addModelRequestInfo(
        ModelRequestInfo(
            startedAt = startedAt,
            finishedAt = finishedAt,
            inputTokenCount = response.usage.inputTokenCount,
            outputTokenCount = response.usage.outputTokenCount,
        ),
    )

    val message = response.message
    state.addMessage(message)
    return ProcessModelResponse(state, agent, message)
}

private fun Finished.process() = state to result

private suspend fun ExecuteTools.process(): Action {
    val toolExecutionResults = executeToolCalls(agent, toolCalls)

    toolExecutionResults.filterIsInstance<ToolExecutionResult.ToolResult>().forEach {
        state.addMessage(it.message)
    }

    val finishedToolResult = toolExecutionResults.filterIsInstance<ToolExecutionResult.FinishedToolResult>().firstOrNull()

    return if (finishedToolResult != null) {
        Finished(state, agent, finishedToolResult.result)
    } else {
        SendModelRequest(state, agent)
    }
}

private fun initializeStartMessages(agent: Agent): List<Message> =
    listOf(agent.systemPromptBuilder.buildSystemPrompt(agent)) +
        agent.contexts.map {
            when (it) {
                is Context.Url -> Message.Url(sender = Sender.Agent, url = it.url, mimeType = it.mimeType)
                is Context.Base64 -> Message.Base64(sender = Sender.Agent, base64Content = it.base64, mimeType = it.mimeType)
                is Context.Text -> Message.Text(Sender.Agent, it.text)
            }
        }

private suspend fun Agent.sendModelRequest(state: State): ModelResponse =
    model.sendRequest(state.messages.replayCache, tools.values.toList() + internalTools.values.toList())

sealed interface Action {
    data class Initialize(val state: State, val agent: Agent) : Action

    data class ExecuteTools(val state: State, val agent: Agent, val toolCalls: List<ToolCall>) : Action

    data class SendModelRequest(val state: State, val agent: Agent) : Action

    data class ProcessModelResponse(val state: State, val agent: Agent, val responseMessage: Message) : Action

    data class Finished(val state: State, val agent: Agent, val result: Result) : Action
}
