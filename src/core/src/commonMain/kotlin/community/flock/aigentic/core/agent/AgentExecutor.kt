package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.tool.FinishReason
import community.flock.aigentic.core.agent.tool.FinishedOrStuck
import community.flock.aigentic.core.agent.tool.finishOrStuckTool
import community.flock.aigentic.core.message.*
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.Tool
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.Clock

interface ToolInterceptor { suspend fun intercept(agent: Agent, tool: Tool, toolCall: ToolCall) }

class AgentExecutor(val toolInterceptors: List<ToolInterceptor> = emptyList()) {
    val agents: MutableList<Agent> = mutableListOf()
    val startedAgents = MutableSharedFlow<String>(replay = 100)

    suspend fun start() {
        agents
            .filter { it.status.value.runningState == AgentRunningState.WAITING_TO_START }
            .forEach {
                runAgent(it)
            }
    }

    suspend fun runAgent(agent: Agent): FinishedOrStuck {
        agent.setRunningState(AgentRunningState.RUNNING)
        startedAgents.emit(agent.id)
        agents.add(agent)

        agent.initialize() // Maybe move to Agent builder?
        val modelResponse = agent.sendModelRequest()

        val result = CompletableDeferred<FinishedOrStuck>()
        processResponse(agent, modelResponse) { result.complete(it) }
        val resultState = result.await()

        agent.updateStatus {
            val endRunningState = if (resultState.reason is FinishReason.ImStuck) {
                AgentRunningState.STUCK
            } else {
                AgentRunningState.COMPLETED
            }
            it.copy(
                runningState = endRunningState,
                endTimestamp = Clock.System.now()
            )
        }
        return resultState
    }

    fun getAgent(agentId: String?): Agent = agents.first { it.id == agentId }

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
        toolInterceptors.forEach { it.intercept(this, tool, toolCall) }
        setRunningState(AgentRunningState.EXECUTING_TOOL)
        val result = tool.handler(functionArgs)
        setRunningState(AgentRunningState.RUNNING)
        return Message.ToolResult(toolCall.id, toolCall.name, ToolResultContent(result))
    }

    private suspend fun sendToolResponse(agent: Agent, onFinished: (FinishedOrStuck) -> Unit) {
        val response = agent.sendModelRequest()
        processResponse(agent, response, onFinished)
    }

    private suspend fun Agent.sendModelRequest(): ModelResponse =
        model.sendRequest(messages.replayCache, tools.values.toList() + internalTools.values.toList())

    fun loadAgents(agents: MutableList<Agent>) {
        this.agents.addAll(agents)
    }
}

suspend fun Agent.updateStatus(update: (currentStatus: AgentStatus) -> AgentStatus) {
    update.invoke(status.value).let {
        status.emit(it)
    }
}

suspend fun Agent.setRunningState(state: AgentRunningState) {
    updateStatus { status.value.copy(runningState = state) }
}