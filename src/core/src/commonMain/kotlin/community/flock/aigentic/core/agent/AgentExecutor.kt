package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.Action.ExecuteTools
import community.flock.aigentic.core.agent.Action.Finished
import community.flock.aigentic.core.agent.Action.Initialize
import community.flock.aigentic.core.agent.Action.ProcessModelResponse
import community.flock.aigentic.core.agent.Action.SendModelRequest
import community.flock.aigentic.core.agent.message.correctionMessage
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
import kotlinx.datetime.Clock

suspend fun Agent.start(): Run =
    coroutineScope {
        check(agentNotStarted()) { "Agent already started" }

        val logging = async { getStatus().map { it.text }.collect(::println) }
        val startedAt = Clock.System.now()
        val finishedOrStuck =
            executeAction(Initialize(this@start)).also {
                delay(10) // Allow some time for the logging to finish
                logging.cancelAndJoin()
            }

        Run(
            startedAt = startedAt,
            finishedAt = Clock.System.now(),
            messages = messages.replayCache,
            result = finishedOrStuck,
        )
    }

private suspend fun executeAction(action: Action): FinishedOrStuck =
    when (action) {
        is Initialize -> executeAction(action.process())
        is SendModelRequest -> executeAction(action.process())
        is ProcessModelResponse -> executeAction(action.process())
        is ExecuteTools -> executeAction(action.process())
        is Finished -> action.process()
    }

private suspend fun Initialize.process(): Action {
    agent.addMessages(initializeStartMessages(agent))
    return SendModelRequest(agent)
}

private suspend fun ProcessModelResponse.process(): Action =
    when (responseMessage) {
        is Message.ToolCalls -> ExecuteTools(agent, responseMessage.toolCalls)
        else -> {
            agent.messages.emit(correctionMessage)
            SendModelRequest(agent)
        }
    }

private suspend fun SendModelRequest.process(): ProcessModelResponse {
    val message = agent.sendModelRequest().message
    agent.addMessage(message)
    return ProcessModelResponse(agent, message)
}

private fun Finished.process() = finishedOrStuck

private suspend fun ExecuteTools.process(): Action {
    val toolResults = executeToolCalls(agent, toolCalls)
    val finished = toolResults.filterIsInstance<ToolExecutionResult.FinishedToolResult>().firstOrNull()

    return if (finished != null) {
        Finished(agent, finished.reason)
    } else {
        agent.addMessages(toolResults.filterIsInstance<ToolExecutionResult.ToolResult>().map { it.message })
        SendModelRequest(agent)
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

private suspend fun Agent.addMessages(messages: List<Message>) = messages.forEach { addMessage(it) }

private suspend fun Agent.addMessage(message: Message) = this.messages.emit(message)

private suspend fun Agent.sendModelRequest(): ModelResponse =
    model.sendRequest(messages.replayCache, tools.values.toList() + internalTools.values.toList())

private sealed interface Action {
    data class Initialize(val agent: Agent) : Action

    data class ExecuteTools(val agent: Agent, val toolCalls: List<ToolCall>) : Action

    data class SendModelRequest(val agent: Agent) : Action

    data class ProcessModelResponse(val agent: Agent, val responseMessage: Message) : Action

    data class Finished(val agent: Agent, val finishedOrStuck: FinishedOrStuck) : Action
}

private fun Agent.agentNotStarted() = messages.replayCache.isEmpty()
