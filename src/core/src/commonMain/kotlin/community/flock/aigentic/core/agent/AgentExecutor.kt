package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.Action.ExecuteTools
import community.flock.aigentic.core.agent.Action.Finished
import community.flock.aigentic.core.agent.Action.Initialize
import community.flock.aigentic.core.agent.Action.ProcessModelResponse
import community.flock.aigentic.core.agent.Action.SendModelRequest
import community.flock.aigentic.core.agent.message.correctionMessage
import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.state.State
import community.flock.aigentic.core.agent.state.addExampleRun
import community.flock.aigentic.core.agent.state.addMessage
import community.flock.aigentic.core.agent.state.addMessages
import community.flock.aigentic.core.agent.state.addModelRequestInfo
import community.flock.aigentic.core.agent.state.getStatus
import community.flock.aigentic.core.agent.state.toRun
import community.flock.aigentic.core.agent.status.AgentStatus
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.exception.AigenticException
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.ContextMessage
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.mapToTextMessages
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.platform.RunSentResult
import community.flock.aigentic.core.util.withStartFinishTiming
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map

suspend fun <I, O> Agent<I, O>.start(): Run =
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

private suspend fun <I, O> publishRun(
    agent: Agent<I, O>,
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

suspend fun <I, O> executeAction(action: Action<I, O>): Pair<State, Result> =
    when (action) {
        is Initialize -> executeAction<I, O>(action.process())
        is SendModelRequest -> executeAction<I, O>(action.process())
        is ProcessModelResponse -> executeAction<I, O>(action.process())
        is ExecuteTools -> executeAction<I, O>(action.process())
        is Finished -> action.process()
    }

private suspend fun <I, O> Initialize<I, O>.process(): Action<I, O> {
    state.addMessages(initializeStartMessages(agent))
    if (agent.tags.isNotEmpty()) {
        state.addMessages(prependWithExampleMessages())
    }
    return SendModelRequest(state, agent)
}

private suspend fun <I, O> Initialize<I, O>.prependWithExampleMessages(): List<Message> {
    val runs = fetchRuns()
    state.addMessage(
        Message.ExampleToolMessage(
            sender = Sender.Agent,
            text =
                """
        |The messages below are example run messages. Each example run has a clearly marked start and end.
        |The example messages continue until you encounter: <END_OF_ALL_EXAMPLES>
                """.trimMargin(),
        ),
    )
    val runMessages: List<List<Message>> =
        runs.mapIndexed { index, run ->
            val messages = run.second.messages
            state.addExampleRun(run.first)
            val textMessages = messages.mapToTextMessages()
            val exampleMessageDescription =
                listOf(
                    Message.ExampleToolMessage(
                        sender = Sender.Agent,
                        text =
                            """
        |The messages below are part of example $index. The example ends when you encounter: <END_EXAMPLE_$index>
                            """.trimMargin(),
                    ),
                )
            val exampleEndMessageDescription =
                listOf(
                    Message.ExampleToolMessage(
                        sender = Sender.Agent,
                        text =
                            """
        |<END_EXAMPLE_$index>.
                            """.trimMargin(),
                    ),
                )
            try {
                exampleMessageDescription
                    .plus(listOf(messages.filterIsInstance<ContextMessage>().first().toExampleMessage()))
                    .plus(textMessages)
                    .plus(exampleEndMessageDescription)
                    .mapNotNull { it }
            } catch (e: Exception) {
                println("test" + e.message)
                return emptyList()
            }
        }
    val finalExampleMessageDescription =
        listOf(
            Message.ExampleToolMessage(
                sender = Sender.Agent,
                text =
                    """
        |<END_OF_ALL_EXAMPLES>
        |All of the previous example messages are to be considered as the results of a desired run.
        |Carefully analyze the relationship between the input (instructions, tool calls and arguments) and the output (responses).
        |Use these relations in the current task and make sure to apply the instructions below to come to the same relationships.
        |All messages following are the input for the current task.
                    """.trimMargin(),
            ),
        )
    val allMessages = runMessages.flatMap { it }
    return allMessages
        .plus(finalExampleMessageDescription)
}

private fun ContextMessage.toExampleMessage(): Message =
    when (this) {
        is Message.Base64 ->
            Message.Base64(
                sender = sender,
                messageType = MessageType.Example,
                base64Content = base64Content,
                mimeType = mimeType,
            )

        is Message.Text ->
            Message.Text(
                sender = sender,
                messageType = MessageType.Example,
                text = text,
            )

        is Message.Url ->
            Message.Url(
                sender = sender,
                messageType = MessageType.Example,
                url = url,
                mimeType = mimeType,
            )

        is Message.ExampleToolMessage ->
            Message.ExampleToolMessage(
                sender = sender,
                text = text,
                id = id,
            )
    }

private suspend fun <I, O> Initialize<I, O>.fetchRuns(): List<Pair<RunId, Run>> =
    runCatching {
        agent.platform
            ?.getRuns(agent.tags)
    }.onFailure {
        state.addMessages(initializeStartMessages(agent))
        aigenticException(it.message.toString())
    }.getOrNull() ?: emptyList()

private suspend fun <I, O> ProcessModelResponse<I, O>.process(): Action<I, O> =
    when (responseMessage) {
        is Message.ToolCalls -> ExecuteTools(state, agent, responseMessage.toolCalls)
        else -> {
            state.messages.emit(correctionMessage)
            SendModelRequest(state, agent)
        }
    }

private suspend fun <I, O> SendModelRequest<I, O>.process(): ProcessModelResponse<I, O> {
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
            thinkingOutputTokenCount = response.usage.thinkingOutputTokenCount,
            cachedInputTokenCount = response.usage.cachedInputTokenCount,
        ),
    )

    val message = response.message
    state.addMessage(message)
    return ProcessModelResponse(state, agent, message)
}

private fun <I, O> Finished<I, O>.process() = state to result

private suspend fun <I, O> ExecuteTools<I, O>.process(): Action<I, O> {
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

private fun <I, O> initializeStartMessages(agent: Agent<I, O>): List<Message> =
    listOf(agent.systemPromptBuilder.buildSystemPrompt(agent)) +
        agent.contexts.map {
            when (it) {
                is Context.Url -> Message.Url(sender = Sender.Agent, url = it.url, mimeType = it.mimeType, messageType = MessageType.New)
                is Context.Base64 ->
                    Message.Base64(
                        sender = Sender.Agent,
                        base64Content = it.base64,
                        mimeType = it.mimeType,
                        messageType = MessageType.New,
                    )

                is Context.Text -> Message.Text(Sender.Agent, messageType = MessageType.New, it.text)
            }
        }

private suspend fun <I, O> Agent<I, O>.sendModelRequest(state: State): ModelResponse =
    model.sendRequest(state.messages.replayCache, tools.values.toList() + internalTools.values.toList())

sealed interface Action<I, O> {
    data class Initialize<I, O>(val state: State, val agent: Agent<I, O>) : Action<I, O>

    data class ExecuteTools<I, O>(val state: State, val agent: Agent<I, O>, val toolCalls: List<ToolCall>) : Action<I, O>

    data class SendModelRequest<I, O>(val state: State, val agent: Agent<I, O>) : Action<I, O>

    data class ProcessModelResponse<I, O>(val state: State, val agent: Agent<I, O>, val responseMessage: Message) : Action<I, O>

    data class Finished<I, O>(val state: State, val agent: Agent<I, O>, val result: Result) : Action<I, O>
}
