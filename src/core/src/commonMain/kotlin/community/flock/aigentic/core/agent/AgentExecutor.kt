package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.tool.FinishedOrStuck
import community.flock.aigentic.core.agent.tool.finishOrStuckTool
import community.flock.aigentic.core.message.*
import community.flock.aigentic.core.tool.DefaultToolPermissionHandler
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.ToolPermissionHandler
import community.flock.aigentic.core.model.ModelResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow

class AgentExecutor(
    val schedules: List<Schedule>,
    val permissionHandler: ToolPermissionHandler = DefaultToolPermissionHandler()
) {
    private val listeners: MutableList<suspend (event: Pair<String, Message>) -> Unit> = mutableListOf()

    suspend fun start() =
        schedules
            .flatMap { it.agents }
            .forEach { runAgent(it) }

    suspend fun runAgent(agent: Agent): FinishedOrStuck {
        applyListeners(agent)
        agent.initialize() // Maybe move to Agent builder?
        val modelResponse = agent.sendModelRequest()

        val result = CompletableDeferred<FinishedOrStuck>()
        processResponse(agent, modelResponse) { result.complete(it) }
        return result.await()
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

    private suspend fun processResponse(agent: Agent, response: ModelResponse, onFinished: (FinishedOrStuck) -> Unit) {
        val message = response.message
        agent.messages.emit(message)

        when (message) {
            is Message.ToolCalls -> {
                val shouldSendNextRequest = message.toolCalls
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

            else -> error("Expected ToolCalls message, got $message")
        }
    }

    private suspend fun Agent.execute(toolCall: ToolCall): Message.ToolResult {
        val functionArgs = toolCall.argumentsAsJson()
        val tool = tools[ToolName(toolCall.name)] ?: error("Tool not registered: $toolCall")
        while (!permissionHandler.hasPermission(tool.toolConfiguration, toolCall)) {
            println("Waiting for permission for ${toolCall.name}")
            delay(300)
        }
        val result = tool.handler(functionArgs)
        return Message.ToolResult(toolCall.id, toolCall.name, ToolResultContent(result))
    }

    private suspend fun sendToolResponse(agent: Agent, onFinished: (FinishedOrStuck) -> Unit) {
        val response = agent.sendModelRequest()
        processResponse(agent, response, onFinished)
    }

    private suspend fun Agent.sendModelRequest(): ModelResponse =
        model.sendRequest(messages.replayCache, tools.values.toList() + internalTools.values.toList())

    fun getMessages(): Map<String, MutableSharedFlow<Message>> =
        schedules.flatMap { it.agents }.associate { it.id to it.messages }

    fun addListener(function: suspend (event: Pair<String, Message>) -> Unit) {
        listeners.add(function)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun applyListeners(agent: Agent) {
        listeners.forEach { function ->
            GlobalScope.launch {
                agent.messages.collect { function.invoke(Pair(agent.id, it)) }
            }
        }
    }
}

class Schedule(
    val agents: List<Agent>,
    val type: ScheduleType
)

sealed interface ScheduleType {
    /**
     * Just run a single time
     */
    data object Single : ScheduleType
}


//suspend fun Agent.execute() = runAgent(this)
