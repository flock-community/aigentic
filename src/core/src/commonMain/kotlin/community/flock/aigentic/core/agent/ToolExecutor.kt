package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.ToolExecutionResult.FinishedToolResult
import community.flock.aigentic.core.agent.ToolExecutionResult.ToolResult
import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.agent.tool.STUCK_WITH_TASK_TOOL_NAME
import community.flock.aigentic.core.agent.tool.stuckWithTaskTool
import community.flock.aigentic.core.exception.aigenticException
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
        FINISHED_TASK_TOOL_NAME -> {
            val finishedResult = agent.finishedTaskTool.handler(toolCall.argumentsAsJson())
            FinishedToolResult(result = finishedResult)
        }

        STUCK_WITH_TASK_TOOL_NAME -> {
            val stuckResult = stuckWithTaskTool.handler(toolCall.argumentsAsJson())
            FinishedToolResult(result = stuckResult)
        }

        else -> {
            val tool = agent.tools[ToolName(toolCall.name)] ?: aigenticException("Tool not registered: $toolCall")
            val result = tool.handler(toolCall.argumentsAsJson())
            ToolResult(Message.ToolResult(toolCall.id, toolCall.name, ToolResultContent(result)))
        }
    }

sealed interface ToolExecutionResult {
    data class FinishedToolResult(val result: Result) : ToolExecutionResult

    data class ToolResult(val message: Message.ToolResult) : ToolExecutionResult
}
