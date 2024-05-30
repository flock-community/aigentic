package community.flock.aigentic.openai.mapper

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.FunctionCall
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.chat.ToolId
import com.aallam.openai.api.core.Role
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.message.ToolResultContent
import com.aallam.openai.api.chat.ToolCall as OpenAIToolCall

object DomainMapper {
    internal fun ChatMessage.toMessage(): Message =
        when {
            isSystemMessage() -> Message.SystemPrompt(content!!)
            isToolCallsMessage() -> Message.ToolCalls(toolCalls.mapToolCalls())
            isToolResult() ->
                Message.ToolResult(
                    toolCallId = ToolCallId(this.toolCallId!!.id),
                    toolName = this.name!!,
                    response = ToolResultContent(this.content!!),
                )

            isTextMessage() ->
                Message.Text(
                    sender = role.mapToSender(),
                    text = content!!,
                )

            else -> aigenticException("Cannot map OpenAI ChatMessage, unknown type: $this")
        }

    private fun ChatMessage.isTextMessage() = messageContent?.let { it is TextContent } ?: false

    private fun ChatMessage.isToolResult() = role == ChatRole.Tool && toolCallId != null

    private fun ChatMessage.isToolCallsMessage() = role == ChatRole.Assistant && toolCalls?.isNotEmpty() ?: false

    private fun ChatMessage.isSystemMessage() = role == ChatRole.System

    private fun List<OpenAIToolCall>?.mapToolCalls(): List<ToolCall> =
        this
            ?.map { it as OpenAIToolCall.Function }
            ?.map {
                ToolCall(
                    id = ToolCallId(it.id.id),
                    name = it.function.name,
                    arguments = it.function.arguments,
                )
            }
            ?: emptyList()

    private fun ChatRole.mapToSender(): Sender =
        when (this) {
            Role.Assistant -> Sender.Model
            Role.User -> Sender.Aigentic
            else -> aigenticException("Unexpected role: $this")
        }
}

object OpenAIMapper {
    internal fun Message.toOpenAIMessage(): ChatMessage {
        val role = mapChatTextRole()
        return when (this) {
            is Message.SystemPrompt -> ChatMessage(role, prompt)
            is Message.Text -> ChatMessage(role, text)
            is Message.Image -> ChatMessage(role = role, listOf(ImagePart(image)))
            is Message.ToolCalls ->
                ChatMessage(
                    role = role,
                    content = null as String?,
                    toolCalls = mapToolCalls(),
                )

            is Message.ToolResult ->
                ChatMessage(
                    role = role,
                    toolCallId = ToolId(toolCallId.id),
                    name = toolName,
                    content = response.result,
                )
        }
    }

    private fun Message.mapChatTextRole(): ChatRole =
        when (this) {
            is Message.SystemPrompt -> ChatRole.System
            is Message.Text -> mapChatTextRole()
            is Message.ToolCalls -> ChatRole.Assistant
            is Message.ToolResult -> ChatRole.Tool
            is Message.Image -> mapImageTextRole()
        }

    private fun Message.Text.mapChatTextRole() =
        when (this.sender) {
            Sender.Aigentic -> ChatRole.User
            Sender.Model -> ChatRole.Assistant
        }

    private fun Message.Image.mapImageTextRole() =
        when (this.sender) {
            Sender.Aigentic -> ChatRole.User
            Sender.Model -> ChatRole.Assistant
        }

    private fun Message.ToolCalls.mapToolCalls(): List<OpenAIToolCall> =
        toolCalls
            .map {
                OpenAIToolCall.Function(
                    id = ToolId(it.id.id),
                    function = FunctionCall(it.name, it.arguments),
                )
            }
}
