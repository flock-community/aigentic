package community.flock.aigentic.core.agent.status

import community.flock.aigentic.core.agent.tool.FinishReason
import community.flock.aigentic.core.agent.tool.finishOrStuckTool
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.argumentsAsJson

sealed interface AgentStatus {
    val text: String

    data object Started : AgentStatus {
        override val text: String = "🛫 Agent started!"
    }

    data class Finished(val summary: String) : AgentStatus {
        override val text: String = "🏁 Agent finished! Summary: $summary"
    }

    data class Stuck(val reason: String) : AgentStatus {
        override val text: String = "🤯 Agent stuck! Reason: $reason"
    }

    data class ExecuteTool(val tool: ToolCall) : AgentStatus {
        override val text = "🏗 Executing tool: ${tool.name} arguments: ${tool.arguments}"
    }

    data class ToolResult(val result: Message.ToolResult) : AgentStatus {
        override val text = "🦾 Tool result ${result.toolName}: ${result.response.result}"
    }
}

suspend fun Message.toStatus(): List<AgentStatus> =
    when (this) {
        is Message.SystemPrompt -> listOf(AgentStatus.Started)
        is Message.Text, is Message.Image -> emptyList()
        is Message.ToolCalls ->
            this.toolCalls.map {
                when (it.name) {
                    finishOrStuckTool.name.value -> getFinishEvent(it)
                    else -> AgentStatus.ExecuteTool(it)
                }
            }
        is Message.ToolResult -> listOf(AgentStatus.ToolResult(this))
    }

suspend fun getFinishEvent(it: ToolCall): AgentStatus {
    val finishedOrStuck = finishOrStuckTool.handler(it.argumentsAsJson())
    return when (finishedOrStuck.reason) {
        FinishReason.FinishedTask ->
            AgentStatus.Finished(finishedOrStuck.description)

        FinishReason.ImStuck ->
            AgentStatus.Stuck(finishedOrStuck.description)
    }
}
