package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.Action.ExecuteTools
import community.flock.aigentic.core.agent.Action.Finished
import community.flock.aigentic.core.agent.Action.Initialize
import community.flock.aigentic.core.agent.Action.ProcessModelResponse
import community.flock.aigentic.core.agent.Action.SendModelRequest
import community.flock.aigentic.core.agent.message.correctionMessage
import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.state.State
import community.flock.aigentic.core.agent.state.addConfigContextMessage
import community.flock.aigentic.core.agent.state.addExampleMessage
import community.flock.aigentic.core.agent.state.addExampleRunId
import community.flock.aigentic.core.agent.state.addModelRequestInfo
import community.flock.aigentic.core.agent.state.addRunContextMessage
import community.flock.aigentic.core.agent.state.addRunExecutionMessage
import community.flock.aigentic.core.agent.state.addSystemPromptMessage
import community.flock.aigentic.core.agent.state.getStatus
import community.flock.aigentic.core.agent.state.toRun
import community.flock.aigentic.core.agent.status.AgentStatus
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.exception.AigenticException
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.ContextMessage
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Message.Base64
import community.flock.aigentic.core.message.Message.ExampleToolMessage
import community.flock.aigentic.core.message.Message.Text
import community.flock.aigentic.core.message.Message.Url
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.asJson
import community.flock.aigentic.core.message.mapToTextMessages
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.platform.RunSentResult
import community.flock.aigentic.core.platform.getRuns
import community.flock.aigentic.core.platform.sendRun
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.util.withStartFinishTiming
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

suspend inline fun <reified I : Any, reified O : Any> Agent<I, O>.start(vararg attachments: Attachment): AgentRun<O> = start(null, *attachments)

suspend inline fun <reified I : Any, reified O : Any> Agent<I, O>.start(
    input: I? = null,
    vararg attachments: Attachment,
): AgentRun<O> =
    coroutineScope {
        val agent = this@start
        val state = State()
        state.events.emit(AgentStatus.Started)
        val logging = async { state.getStatus().map { it.text }.collect(::println) }
        try {
            val run = executeAction(Initialize(state, agent, input, attachments.toList())).toRun()
            publishRun(agent, run, state)
            run
        } catch (e: AigenticException) {
            state.events.emit(AgentStatus.Fatal(e.message))
            val run = (state to Outcome.Fatal(e.message)).toRun<O>()
            publishRun(agent, run, state)
            run
        } finally {
            delay(10) // Allow some time for the logging to finish
            logging.cancelAndJoin()
        }
    }

@PublishedApi
internal suspend inline fun <reified I : Any, reified O : Any> publishRun(
    agent: Agent<I, O>,
    run: AgentRun<O>,
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

suspend inline fun <reified I : Any, reified O : Any> executeAction(action: Action<I, O>): Pair<State, Outcome<O>> {
    var currentAction = action
    while (true) {
        currentAction =
            when (val actionToProcess = currentAction) {
                is Initialize -> actionToProcess.process()
                is SendModelRequest -> actionToProcess.process()
                is ProcessModelResponse -> actionToProcess.process()
                is ExecuteTools -> actionToProcess.process()
                is Finished -> return actionToProcess.process()
            }
    }
}

@PublishedApi
internal suspend inline fun <reified I : Any, reified O : Any> Initialize<I, O>.process(): Action<I, O> {
    seedInitialMessages()

    if (agent.tags.isNotEmpty()) {
        val (messages, runIds) = fetchExampleRunMessages()
        messages.forEach { state.addExampleMessage(it) }
        runIds.forEach { state.addExampleRunId(it) }
    }

    return SendModelRequest(
        state = state,
        agent = agent,
        structuredResponseParameter = agent.responseParameter.takeIf { agent.isStructuredOutputAgent() },
    )
}

@PublishedApi
internal suspend inline fun <reified I : Any, reified O : Any> Initialize<I, O>.fetchExampleRunMessages(): Pair<List<Message>, List<RunId>> {
    val runs = fetchRuns()
    if (runs.isEmpty()) return emptyList<Message>() to emptyList()

    val messages =
        buildList {
            add(
                ExampleToolMessage(
                    sender = Sender.Agent,
                    text =
                        """
                |The messages below are example run messages. Each example run has a clearly marked start and end.
                |The example messages continue until you encounter: <END_OF_ALL_EXAMPLES>
                        """.trimMargin(),
                ),
            )

            runs.forEachIndexed { index, (_, run) ->
                add(
                    ExampleToolMessage(
                        sender = Sender.Agent,
                        text = "The messages below are part of example $index. The example ends when you encounter: <END_EXAMPLE_$index>",
                    ),
                )
                run.messages.filterIsInstance<ContextMessage>().firstOrNull()?.toExampleMessage()?.let { add(it) }
                addAll(run.messages.mapToTextMessages())
                add(
                    ExampleToolMessage(
                        sender = Sender.Agent,
                        text = "<END_EXAMPLE_$index>.",
                    ),
                )
            }

            add(
                ExampleToolMessage(
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
        }

    return messages to runs.map { it.first }
}

@PublishedApi
internal fun ContextMessage.toExampleMessage(): Message = this as Message

@PublishedApi
internal suspend inline fun <reified I : Any, reified O : Any> Initialize<I, O>.fetchRuns(): List<Pair<RunId, Run<O>>> =
    agent.platform?.let { platform ->
        runCatching {
            platform.getRuns<O>(agent.tags)
        }.getOrElse { error ->
            aigenticException("Failed to fetch example runs: ${error.message}")
        }
    } ?: aigenticException("Platform must be configured when using agent tags for example runs")

@PublishedApi
internal suspend inline fun <reified I : Any, reified O : Any> ProcessModelResponse<I, O>.process(): Action<I, O> =
    when (responseMessage) {
        is Message.ToolCalls -> ExecuteTools(state, agent, responseMessage.toolCalls)
        is Message.StructuredOutput -> {
            val response = Json.decodeFromJsonElement<O>(responseMessage.asJson())
            val outcome = Outcome.Finished("Finished", response)
            Finished(state, agent, outcome)
        }

        else -> {
            state.addRunExecutionMessage(correctionMessage)
            SendModelRequest(state, agent, null)
        }
    }

@PublishedApi
internal suspend inline fun <reified I : Any, reified O : Any> SendModelRequest<I, O>.process(): ProcessModelResponse<I, O> {
    val (startedAt, finishedAt, response) =
        withStartFinishTiming {
            agent.sendModelRequest(state, structuredResponseParameter)
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
    state.addRunExecutionMessage(message)

    return ProcessModelResponse(
        state = state,
        agent = agent,
        responseMessage = message,
    )
}

@PublishedApi
internal inline fun <reified I : Any, reified O : Any> Finished<I, O>.process() = state to outcome

@PublishedApi
internal suspend inline fun <reified I : Any, reified O : Any> ExecuteTools<I, O>.process(): Action<I, O> {
    val toolExecutionResults = executeToolCalls<I, O>(agent, toolCalls)

    toolExecutionResults.filterIsInstance<ToolExecutionResult.ToolResult>().forEach {
        state.addRunExecutionMessage(it.message)
    }

    val finishedToolResult = toolExecutionResults.filterIsInstance<ToolExecutionResult.FinishedToolResult<O>>().firstOrNull()

    return if (finishedToolResult != null) {
        Finished(state, agent, finishedToolResult.outcome)
    } else {
        SendModelRequest(state, agent, null)
    }
}

@PublishedApi
internal suspend inline fun <reified I : Any, reified O : Any> Initialize<I, O>.seedInitialMessages() {
    state.addSystemPromptMessage(agent.getSystemPromptMessage())
    agent.contexts.map { it.toMessage() }.forEach { state.addConfigContextMessage(it) }
    runAttachments.map { it.toMessage() }.forEach { state.addRunContextMessage(it) }
    taskInput?.let { createTaskInputMessage(it) }?.let { state.addRunContextMessage(it) }
}

@PublishedApi
internal fun Context.toMessage(): Message =
    when (this) {
        is Context.Url ->
            Url(
                sender = Sender.Agent,
                url = url,
                mimeType = mimeType,
            )

        is Context.Base64 ->
            Base64(
                sender = Sender.Agent,
                base64Content = base64,
                mimeType = mimeType,
            )

        is Context.Text ->
            Text(
                sender = Sender.Agent,
                text = text,
            )
    }

@PublishedApi
internal fun Attachment.toMessage(): Message =
    when (this) {
        is Attachment.Base64 ->
            Base64(
                sender = Sender.Agent,
                base64Content = base64Content,
                mimeType = mimeType,
            )

        is Attachment.Url ->
            Url(
                sender = Sender.Agent,
                url = url,
                mimeType = mimeType,
            )
    }

@PublishedApi
internal inline fun <reified I : Any> createTaskInputMessage(input: I): Text =
    Text(
        sender = Sender.Agent,
        text = if (input is String) input else Json.encodeToString(input),
    )

@PublishedApi
internal suspend inline fun <I : Any, reified O : Any> Agent<I, O>.sendModelRequest(
    state: State,
    structuredResponseParameter: Parameter?,
): ModelResponse =
    model.sendRequest(
        messages = state.messages.snapshot(),
        tools = tools.values.toList() + internalTools<O>().values.toList(),
        structuredOutputParameter = structuredResponseParameter,
    )

sealed interface Action<I : Any, O : Any> {
    data class Initialize<I : Any, O : Any>(
        val state: State,
        val agent: Agent<I, O>,
        val taskInput: I?,
        val runAttachments: List<Attachment>,
    ) : Action<I, O>

    data class ExecuteTools<I : Any, O : Any>(
        val state: State,
        val agent: Agent<I, O>,
        val toolCalls: List<ToolCall>,
    ) : Action<I, O>

    data class SendModelRequest<I : Any, O : Any>(
        val state: State,
        val agent: Agent<I, O>,
        val structuredResponseParameter: Parameter?,
    ) : Action<I, O>

    data class ProcessModelResponse<I : Any, O : Any>(
        val state: State,
        val agent: Agent<I, O>,
        val responseMessage: Message,
    ) : Action<I, O>

    data class Finished<I : Any, O : Any>(
        val state: State,
        val agent: Agent<I, O>,
        val outcome: Outcome<O>,
    ) : Action<I, O>
}
