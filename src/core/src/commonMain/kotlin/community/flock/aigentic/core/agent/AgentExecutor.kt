package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.events.toEvents
import community.flock.aigentic.core.agent.tool.FinishReason
import community.flock.aigentic.core.agent.tool.FinishedOrStuck
import community.flock.aigentic.core.agent.tool.finishOrStuckTool
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolResultContent
import community.flock.aigentic.core.message.argumentsAsJson
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.datetime.Clock

data class ToolInterceptorResult(val cancelExecution: Boolean, val reason: String?)

interface ToolInterceptor {
    suspend fun intercept(
        agent: Agent,
        tool: Tool,
        toolCall: ToolCall,
    ): ToolInterceptorResult
}

suspend fun Agent.run(): FinishedOrStuck =
    coroutineScope {
        val logging = async {
            getMessages().flatMapConcat { it.toEvents().asFlow() }.collect {
                println(it.text)
            }
        }

        AgentExecutor().runAgent(this@run).also {
            delay(10) // Allow some time for the logging to finish
            logging.cancelAndJoin()
        }
    }

class AgentExecutor(private val toolInterceptors: List<ToolInterceptor> = emptyList()) {
    suspend fun runAgent(agent: Agent): FinishedOrStuck {
        agent.setRunningState(AgentRunningState.RUNNING)

        agent.initialize() // Maybe move to Agent builder?
        val modelResponse = agent.sendModelRequest()

        val result = CompletableDeferred<FinishedOrStuck>()
        processResponse(agent, modelResponse) { result.complete(it) }
        val resultState = result.await()

        agent.updateStatus {
            val endRunningState =
                if (resultState.reason is FinishReason.ImStuck) {
                    AgentRunningState.STUCK
                } else {
                    AgentRunningState.COMPLETED
                }
            it.copy(
                runningState = endRunningState,
                endTimestamp = Clock.System.now(),
            )
        }
        return resultState
    }

    private suspend fun Agent.initialize() {
        internalTools[finishOrStuckTool.name] = finishOrStuckTool
        messages.emit(systemPromptBuilder.buildSystemPrompt(this))
        contexts.map {
            when (it) {
                is Context.Image -> Message.Image(Sender.Aigentic, it.base64)
                is Context.Text -> Message.Text(Sender.Aigentic, it.text)
            }
        }.forEach { messages.emit(it) }
    }

    private suspend fun processResponse(
        agent: Agent,
        response: ModelResponse,
        onFinished: (FinishedOrStuck) -> Unit,
    ) {
        val message = response.message
        agent.messages.emit(message)

        when (message) {
            is Message.ToolCalls -> {
                val shouldSendNextRequest =
                    message.toolCalls
                        .map { toolCall ->
                            when (toolCall.name) {
                                finishOrStuckTool.name.value -> {
                                    val finishedOrStuck = finishOrStuckTool.handler(toolCall.argumentsAsJson())
                                    onFinished(finishedOrStuck)
                                    false
                                }

                                else -> {
                                    val toolResult = agent.execute(toolCall)
                                    agent.messages.emit(toolResult)
                                    true
                                }
                            }
                        }
                        .contains(true)

                if (shouldSendNextRequest) {
                    sendToolResponse(agent, onFinished)
                }
            }

            else ->
                error("Expected ToolCalls message, got $message")
        }
    }

    private suspend fun Agent.execute(toolCall: ToolCall): Message.ToolResult {
        val functionArgs = toolCall.argumentsAsJson()
        val tool = tools[ToolName(toolCall.name)] ?: error("Tool not registered: $toolCall")

        val cancelMessage = runInterceptors(this, tool, toolCall)
        if (cancelMessage != null) {
            setRunningState(AgentRunningState.RUNNING)
            return cancelMessage
        }

        setRunningState(AgentRunningState.EXECUTING_TOOL)
        val result = tool.handler(functionArgs)
        setRunningState(AgentRunningState.RUNNING)
        return Message.ToolResult(toolCall.id, toolCall.name, ToolResultContent(result))
    }

    private suspend fun runInterceptors(
        agent: Agent,
        tool: Tool,
        toolCall: ToolCall,
    ): Message.ToolResult? =
        toolInterceptors
            .map { it.intercept(agent, tool, toolCall) }
            .firstOrNull { it.cancelExecution }?.let {
                Message.ToolResult(
                    toolCall.id,
                    toolCall.name,
                    ToolResultContent(it.reason ?: "Tool execution blocked by interceptor"),
                )
            }

    private suspend fun sendToolResponse(
        agent: Agent,
        onFinished: (FinishedOrStuck) -> Unit,
    ) {
        val response = agent.sendModelRequest()
        processResponse(agent, response, onFinished)
    }

    private suspend fun Agent.sendModelRequest(): ModelResponse =
        model.sendRequest(messages.replayCache, tools.values.toList() + internalTools.values.toList())
}

suspend fun Agent.updateStatus(update: (currentStatus: AgentStatus) -> AgentStatus) {
    update.invoke(status.value).let {
        status.emit(it)
    }
}

suspend fun Agent.setRunningState(state: AgentRunningState) {
    updateStatus { status.value.copy(runningState = state) }
}
