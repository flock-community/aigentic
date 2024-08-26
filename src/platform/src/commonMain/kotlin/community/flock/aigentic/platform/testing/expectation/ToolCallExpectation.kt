import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import community.flock.aigentic.core.agent.tool.STUCK_WITH_TASK_TOOL_NAME
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.platform.testing.ToolCallOverride

data class ToolCallExpectation(
    val toolCall: ToolCall,
    val toolResult: Message.ToolResult,
)

fun createToolCallExpectations(
    toolCallOverrides: List<ToolCallOverride>,
    run: Run,
) = run.messages.filterIsInstance<Message.ToolCalls>().flatMap { toolCallsMessage ->
    toolCallsMessage.toolCalls.mapNotNull { toolCall ->
        when (toolCall.name) {
            FINISHED_TASK_TOOL_NAME, STUCK_WITH_TASK_TOOL_NAME -> null
            else -> createToolCallExpectation(toolCallOverrides, toolCall, run)
        }
    }
}

fun createToolCallExpectation(
    toolCallOverrides: List<ToolCallOverride>,
    originalToolCall: ToolCall,
    run: Run,
): ToolCallExpectation {
    val toolCall =
        toolCallOverrides.find { it.toolCallId == originalToolCall.id }?.let {
            ToolCall(
                id = originalToolCall.id, name = originalToolCall.name, arguments = it.arguments,
            )
        } ?: originalToolCall

    // TODO currently only 1 override per toolcall name
    val toolResult =
        run.messages.filterIsInstance<Message.ToolResult>().find { it.toolCallId == originalToolCall.id } ?: aigenticException(
            "No tool result found for $originalToolCall",
        )

    return ToolCallExpectation(toolCall = toolCall, toolResult = toolResult)
}
