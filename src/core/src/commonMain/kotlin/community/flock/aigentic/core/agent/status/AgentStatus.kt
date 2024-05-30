package community.flock.aigentic.core.agent.status

import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import community.flock.aigentic.core.agent.tool.STUCK_WITH_TASK_TOOL_NAME
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall

sealed interface AgentStatus {
    val text: String

    data object Started : AgentStatus {
        override val text: String = "🛫 Agent started!"
    }

    data object Finished : AgentStatus {
        override val text: String = "🏁 Agent finished!"
    }

    data object Stuck : AgentStatus {
        override val text: String = "🤯 Agent stuck!"
    }

    data class ExecuteTool(val tool: ToolCall) : AgentStatus {
        override val text = "🏗 Executing tool: ${tool.name} arguments: ${tool.arguments}"
    }

    data class ToolResult(val result: Message.ToolResult) : AgentStatus {
        override val text = "🦾 Tool result ${result.toolName}: ${result.response.result}"
    }
}

fun Message.toStatus(): List<AgentStatus> =
    when (this) {
        is Message.SystemPrompt -> listOf(AgentStatus.Started)
        is Message.Text, is Message.Image -> emptyList()
        is Message.ToolCalls ->
            this.toolCalls.map {
                when (it.name) {
                    FINISHED_TASK_TOOL_NAME -> AgentStatus.Finished
                    STUCK_WITH_TASK_TOOL_NAME -> AgentStatus.Stuck
                    else -> AgentStatus.ExecuteTool(it)
                }
            }

        is Message.ToolResult -> listOf(AgentStatus.ToolResult(this))
    }
