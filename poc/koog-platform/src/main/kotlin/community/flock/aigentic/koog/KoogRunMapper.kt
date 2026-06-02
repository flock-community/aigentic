package community.flock.aigentic.koog

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart

internal fun List<Message>.toMessageDtos(): List<MessageDto> = flatMap { it.toMessageDtos() }

private fun Message.toMessageDtos(): List<MessageDto> {
    val createdAt = metaInfo.timestamp.toString()
    return when (this) {
        is Message.System ->
            listOf(SystemPromptMessageDto(createdAt, SenderDto.Agent, textContent(), MessageCategoryDto.SYSTEM_PROMPT))

        is Message.User -> userDtos(createdAt)
        is Message.Assistant -> assistantDtos(createdAt)
    }
}

private fun Message.User.userDtos(createdAt: String): List<MessageDto> {
    val dtos = mutableListOf<MessageDto>()
    val text = parts.filterIsInstance<MessagePart.Text>().joinToString("\n") { it.text }
    if (text.isNotEmpty()) {
        dtos += TextMessageDto(createdAt, SenderDto.Agent, text, MessageCategoryDto.EXECUTION)
    }
    parts.filterIsInstance<MessagePart.Tool.Result>().forEach { result ->
        dtos += ToolResultMessageDto(createdAt, SenderDto.Agent, result.id ?: "", result.tool, result.output, MessageCategoryDto.EXECUTION)
    }
    return dtos
}

private fun Message.Assistant.assistantDtos(createdAt: String): List<MessageDto> {
    val dtos = mutableListOf<MessageDto>()
    val text = parts.filterIsInstance<MessagePart.Text>().joinToString("\n") { it.text }
    if (text.isNotEmpty()) {
        dtos += TextMessageDto(createdAt, SenderDto.Model, text, MessageCategoryDto.EXECUTION)
    }
    val calls = parts.filterIsInstance<MessagePart.Tool.Call>()
    if (calls.isNotEmpty()) {
        dtos += ToolCallsMessageDto(
            createdAt,
            SenderDto.Model,
            calls.map { ToolCallDto(it.id ?: "", it.tool, it.args) },
            MessageCategoryDto.EXECUTION,
        )
    }
    return dtos
}

internal fun List<ToolDescriptor>.toToolDtos(): List<ToolDto> =
    map { tool ->
        ToolDto(
            name = tool.name,
            description = tool.description,
            parameters =
                tool.requiredParameters.map { ParameterDto(it.name, it.description, true, it.type.name) } +
                    tool.optionalParameters.map { ParameterDto(it.name, it.description, false, it.type.name) },
        )
    }
