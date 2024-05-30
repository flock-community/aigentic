package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.tool.FinishedOrStuck
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolResultContent
import community.flock.aigentic.core.message.argumentsAsJson
import community.flock.aigentic.core.tool.ToolName

suspend fun executeToolCalls(
    agent: Agent,
    toolCalls: List<ToolCall>,
): List<ToolExecutionResult> = toolCalls.map { executeTool(agent, it) }

private suspend fun executeTool(
    agent: Agent,
    toolCall: ToolCall,
): ToolExecutionResult =
    when (toolCall.name) {
        agent.finishOrStuckTool.name.value -> {
            val finishedOrStuck = agent.finishOrStuckTool.handler(toolCall.argumentsAsJson())
            ToolExecutionResult.FinishedToolResult(finishedOrStuck)
        }

        else -> {
            val tool = agent.tools[ToolName(toolCall.name)] ?: error("Tool not registered: $toolCall")
            val result = tool.handler(toolCall.argumentsAsJson())
            ToolExecutionResult.ToolResult(Message.ToolResult(toolCall.id, toolCall.name, ToolResultContent(result)))
        }
    }

sealed interface ToolExecutionResult {
    data class FinishedToolResult(val reason: FinishedOrStuck) : ToolExecutionResult

    data class ToolResult(val message: Message.ToolResult) : ToolExecutionResult
}
