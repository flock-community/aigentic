package community.flock.aigentic.core.agent.events

import community.flock.aigentic.core.agent.tool.FinishReason
import community.flock.aigentic.core.agent.tool.finishOrStuckTool
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.argumentsAsJson

sealed interface AgentEvent {
    val text: String

    data object Started : AgentEvent {
        override val text: String = "🛫 Agent started!"
    }

    data class Finished(val summary: String) : AgentEvent {
        override val text: String = "🏁 Agent finished! Summary: $summary"
    }

    data class Stuck(val reason: String) : AgentEvent {
        override val text: String = "🤯 Agent stuck! Reason: $reason"
    }

    data class ExecuteTool(val tool: ToolCall) : AgentEvent {
        override val text = "🏗 Executing tool: ${tool.name} arguments: ${tool.arguments}"
    }

    data class ToolResult(val result: Message.ToolResult) : AgentEvent {
        override val text = "🦾 Tool result ${result.toolName}: ${result.response.result}"
    }

    data object SendingResponse : AgentEvent {
        override val text =
            """
            📡 Sending response to model
            -----------------------------------
            """.trimIndent()
    }
}

suspend fun Message.toEvents(): List<AgentEvent> =
    when (this) {
        is Message.SystemPrompt -> listOf(AgentEvent.Started)
        is Message.Text, is Message.Image -> emptyList()
        is Message.ToolCalls ->
            this.toolCalls.map {
                when (it.name) {
                    finishOrStuckTool.name.value -> getFinishEvent(it)
                    else -> AgentEvent.ExecuteTool(it)
                }
            }
        is Message.ToolResult -> listOf(AgentEvent.ToolResult(this))
    }

suspend fun getFinishEvent(it: ToolCall): AgentEvent {
    val finishedOrStuck = finishOrStuckTool.handler(it.argumentsAsJson())
    return when (finishedOrStuck.reason) {
        FinishReason.FinishedAllTasks ->
            AgentEvent.Finished(finishedOrStuck.description)

        FinishReason.ImStuck ->
            AgentEvent.Stuck(finishedOrStuck.description)
    }
}
