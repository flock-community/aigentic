package community.flock.aigentic.koog.mapper

import ai.koog.agents.core.feature.handler.tool.ToolCallCompletedContext
import ai.koog.prompt.Prompt
import ai.koog.prompt.message.MessagePart
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.message.ToolResultContent
import ai.koog.prompt.message.Message as KoogMessage
import community.flock.aigentic.core.message.Message as AigenticMessage

fun Prompt.systemPromptText(): String = messages.filterIsInstance<KoogMessage.System>().joinToString("\n") { it.textContent() }

fun Prompt.initialUserText(): String? = messages.filterIsInstance<KoogMessage.User>().firstOrNull()?.textContent()

fun KoogMessage.Assistant.toAigenticMessages(): List<AigenticMessage> {
    val toolCalls = parts.filterIsInstance<MessagePart.Tool.Call>()
    return when {
        toolCalls.isNotEmpty() -> {
            listOf(
                AigenticMessage.ToolCalls(
                    toolCalls =
                        toolCalls.map { call ->
                            ToolCall(id = ToolCallId(call.id ?: call.tool), name = call.tool, arguments = call.args)
                        },
                    category = MessageCategory.EXECUTION,
                ),
            )
        }

        textContent().isNotBlank() -> {
            listOf(AigenticMessage.Text(sender = Sender.Model, text = textContent(), category = MessageCategory.EXECUTION))
        }

        else -> {
            emptyList()
        }
    }
}

fun ToolCallCompletedContext.toAigenticToolResultMessage(): AigenticMessage =
    AigenticMessage.ToolResult(
        toolCallId = ToolCallId(toolCallId ?: toolName),
        toolName = toolName,
        response = ToolResultContent(toolResult?.toString() ?: ""),
        category = MessageCategory.EXECUTION,
    )
