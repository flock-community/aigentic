package community.flock.aigentic.koog

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.prompt.message.AttachmentContent
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

internal const val CATEGORY_METADATA_KEY: String = "aigentic.category"

internal fun List<Message>.toMessageDtos(
    configPromptSize: Int = 0,
    structuredFinalResponse: Boolean = false,
): List<MessageDto> {
    val firstAssistantIndex = indexOfFirst { it is Message.Assistant }
    val lastAssistantIndex = indexOfLast { it is Message.Assistant }
    return flatMapIndexed { index, message ->
        val category = message.category(index, configPromptSize, firstAssistantIndex)
        message.toMessageDtos(category, structuredOutput = structuredFinalResponse && index == lastAssistantIndex)
    }
}

private fun Message.category(
    index: Int,
    configPromptSize: Int,
    firstAssistantIndex: Int,
): MessageCategoryDto {
    metaInfo.metadata?.categoryOverride()?.let { return it }
    return when {
        this is Message.System -> MessageCategoryDto.SYSTEM_PROMPT
        index < configPromptSize -> MessageCategoryDto.CONFIG_CONTEXT
        firstAssistantIndex == -1 || index < firstAssistantIndex -> MessageCategoryDto.RUN_CONTEXT
        else -> MessageCategoryDto.EXECUTION
    }
}

private fun JsonObject.categoryOverride(): MessageCategoryDto? =
    (this[CATEGORY_METADATA_KEY] as? JsonPrimitive)
        ?.contentOrNull
        ?.let { name -> runCatching { MessageCategoryDto.valueOf(name) }.getOrNull() }

private fun Message.toMessageDtos(
    category: MessageCategoryDto,
    structuredOutput: Boolean,
): List<MessageDto> {
    val createdAt = metaInfo.timestamp.toString()
    return when (this) {
        is Message.System ->
            listOf(SystemPromptMessageDto(createdAt, SenderDto.Agent, textContent(), category))

        is Message.User -> userDtos(createdAt, category)
        is Message.Assistant -> assistantDtos(createdAt, category, structuredOutput)
    }
}

private fun Message.User.userDtos(
    createdAt: String,
    category: MessageCategoryDto,
): List<MessageDto> {
    val dtos = mutableListOf<MessageDto>()
    val text = parts.filterIsInstance<MessagePart.Text>().joinToString("\n") { it.text }
    if (text.isNotEmpty()) {
        dtos += TextMessageDto(createdAt, SenderDto.Agent, text, category)
    }
    dtos += parts.filterIsInstance<MessagePart.Attachment>().mapNotNull { it.toDto(createdAt, SenderDto.Agent, category) }
    parts.filterIsInstance<MessagePart.Tool.Result>().forEach { result ->
        dtos += ToolResultMessageDto(createdAt, SenderDto.Agent, result.id ?: "", result.tool, result.output, category)
    }
    return dtos
}

private fun Message.Assistant.assistantDtos(
    createdAt: String,
    category: MessageCategoryDto,
    structuredOutput: Boolean,
): List<MessageDto> {
    val dtos = mutableListOf<MessageDto>()
    val text = parts.filterIsInstance<MessagePart.Text>().joinToString("\n") { it.text }
    if (text.isNotEmpty()) {
        dtos +=
            if (structuredOutput) {
                StructuredOutputMessageDto(createdAt, SenderDto.Model, text, category)
            } else {
                TextMessageDto(createdAt, SenderDto.Model, text, category)
            }
    }
    dtos += parts.filterIsInstance<MessagePart.Attachment>().mapNotNull { it.toDto(createdAt, SenderDto.Model, category) }
    val calls = parts.filterIsInstance<MessagePart.Tool.Call>()
    if (calls.isNotEmpty()) {
        dtos += ToolCallsMessageDto(
            createdAt,
            SenderDto.Model,
            calls.map { ToolCallDto(it.id ?: "", it.tool, it.args) },
            category,
        )
    }
    return dtos
}

private fun MessagePart.Attachment.toDto(
    createdAt: String,
    sender: SenderDto,
    category: MessageCategoryDto,
): MessageDto? {
    return when (val content = source.content) {
        is AttachmentContent.PlainText ->
            TextMessageDto(createdAt, sender, content.text, category)

        is AttachmentContent.URL -> {
            val mimeType = source.mimeType.toMimeTypeDto() ?: return null
            UrlMessageDto(createdAt, sender, content.url, mimeType, category)
        }

        is AttachmentContent.Binary -> {
            val mimeType = source.mimeType.toMimeTypeDto() ?: return null
            Base64MessageDto(createdAt, sender, content.asBase64(), mimeType, category)
        }
    }
}

private fun String.toMimeTypeDto(): MimeTypeDto? =
    when (lowercase()) {
        "image/jpeg", "image/jpg" -> MimeTypeDto.IMAGE_JPEG
        "image/png" -> MimeTypeDto.IMAGE_PNG
        "image/webp" -> MimeTypeDto.IMAGE_WEBP
        "image/heic" -> MimeTypeDto.IMAGE_HEIC
        "image/heif" -> MimeTypeDto.IMAGE_HEIF
        "application/pdf" -> MimeTypeDto.APPLICATION_PDF
        else -> null
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
