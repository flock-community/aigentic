package community.flock.aigentic.core.agent.status

import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import community.flock.aigentic.core.agent.tool.STUCK_WITH_TASK_TOOL_NAME
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall

@Suppress("ktlint:standard:max-line-length")
sealed interface AgentStatus {
    val text: String

    data object Started : AgentStatus {
        override val text: String = "🚀 Agent started!"
    }

    data class Finished(val result: String) : AgentStatus {
        override val text: String = "🏁 Agent finished! $result"
    }

    data class Stuck(val result: String) : AgentStatus {
        override val text: String = "🤯 Agent stuck! $result"
    }

    data class Fatal(val reason: String) : AgentStatus {
        override val text: String = "💥 Agent crashed: $reason"
    }

    data class ExecuteTool(val tool: ToolCall) : AgentStatus {
        override val text = "🏗 Executing tool: ${tool.name} arguments: ${tool.arguments}"
    }

    data class ToolResult(val result: Message.ToolResult) : AgentStatus {
        override val text = "🦾 Tool result ${result.toolName}: ${result.response.result}"
    }

    data object PublishedRunSuccess : AgentStatus {
        override val text: String = "🖨️ Published run to Aigentic platform!"
    }

    data object PublishedRunUnauthorized : AgentStatus {
        override val text: String = "🔐 Could not publish run to Aigentic platform, agent unauthorized! Make sure you've configured the correct agent name and secret!"
    }

    data class PublishedRunError(val reason: String) : AgentStatus {
        override val text: String = "💥 Could not publish run to Aigentic platform: $reason"
    }
}

fun Message.toStatus(): List<AgentStatus> =
    when (this) {
        is Message.SystemPrompt -> emptyList()
        is Message.Text, is Message.Url, is Message.Base64, is Message.ExampleToolMessage -> emptyList()
        is Message.ToolCalls ->
            this.toolCalls.map {
                when (it.name) {
                    FINISHED_TASK_TOOL_NAME -> AgentStatus.Finished(it.arguments)
                    STUCK_WITH_TASK_TOOL_NAME -> AgentStatus.Stuck(it.arguments)
                    else -> AgentStatus.ExecuteTool(it)
                }
            }
        is Message.ToolResult -> listOf(AgentStatus.ToolResult(this))
    }
