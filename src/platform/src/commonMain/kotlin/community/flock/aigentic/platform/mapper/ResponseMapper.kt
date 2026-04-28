package community.flock.aigentic.platform.mapper

import community.flock.aigentic.core.agent.AgentRun
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.message.ToolResultContent
import community.flock.aigentic.gateway.wirespec.model.Base64MessageDto
import community.flock.aigentic.gateway.wirespec.model.FatalResultDto
import community.flock.aigentic.gateway.wirespec.model.FinishedResultDto
import community.flock.aigentic.gateway.wirespec.model.MessageCategoryDto
import community.flock.aigentic.gateway.wirespec.model.MimeTypeDto
import community.flock.aigentic.gateway.wirespec.model.RunDetailsDto
import community.flock.aigentic.gateway.wirespec.model.SenderDto
import community.flock.aigentic.gateway.wirespec.model.StructuredOutputMessageDto
import community.flock.aigentic.gateway.wirespec.model.StuckResultDto
import community.flock.aigentic.gateway.wirespec.model.SystemPromptMessageDto
import community.flock.aigentic.gateway.wirespec.model.TextMessageDto
import community.flock.aigentic.gateway.wirespec.model.ToolCallsMessageDto
import community.flock.aigentic.gateway.wirespec.model.ToolResultMessageDto
import community.flock.aigentic.gateway.wirespec.model.UrlMessageDto
import kotlin.time.Instant

internal fun RunDetailsDto.toRun(): AgentRun<String> {
    val mappedMessages =
        messages.map {
            when (it) {
                is Base64MessageDto -> Message.Base64(it.sender.map(), it.base64Content, it.mimeType.map(), it.category.map())
                is SystemPromptMessageDto -> Message.SystemPrompt(it.prompt)
                is TextMessageDto -> Message.Text(it.sender.map(), it.text, it.category.map())
                is StructuredOutputMessageDto -> Message.StructuredOutput(it.response)
                is ToolCallsMessageDto ->
                    Message.ToolCalls(
                        it.toolCalls.map { toolCallDto ->
                            ToolCall(
                                ToolCallId(toolCallDto.id),
                                toolCallDto.name,
                                toolCallDto.arguments,
                            )
                        },
                    )

                is ToolResultMessageDto ->
                    Message.ToolResult(
                        ToolCallId(it.toolCallId),
                        it.toolName,
                        ToolResultContent(it.response),
                    )

                is UrlMessageDto -> Message.Url(it.sender.map(), it.url, it.mimeType.map(), it.category.map())
            }
        }

    val systemPrompt =
        mappedMessages.filterIsInstance<Message.SystemPrompt>().firstOrNull()
            ?: Message.SystemPrompt("You are a helpful AI assistant")

    return AgentRun(
        startedAt = Instant.parse(startedAt),
        finishedAt = Instant.parse(finishedAt),
        messages = mappedMessages,
        outcome =
            when (result) {
                is FatalResultDto -> Outcome.Fatal(result.message)
                is FinishedResultDto -> Outcome.Finished(result.description, result.response)
                is StuckResultDto -> Outcome.Stuck(result.reason)
            },
        modelRequests = listOf(),
        systemPromptMessage = systemPrompt,
    )
}

private fun MessageCategoryDto?.map(): MessageCategory =
    when (this) {
        null -> MessageCategory.EXECUTION
        MessageCategoryDto.SYSTEM_PROMPT -> MessageCategory.SYSTEM_PROMPT
        MessageCategoryDto.CONFIG_CONTEXT -> MessageCategory.CONFIG_CONTEXT
        MessageCategoryDto.RUN_CONTEXT -> MessageCategory.RUN_CONTEXT
        MessageCategoryDto.EXAMPLE -> MessageCategory.EXAMPLE
        MessageCategoryDto.EXECUTION -> MessageCategory.EXECUTION
    }

private fun MimeTypeDto.map() =
    when (this) {
        MimeTypeDto.IMAGE_JPEG -> MimeType.JPEG
        MimeTypeDto.IMAGE_PNG -> MimeType.PNG
        MimeTypeDto.IMAGE_WEBP -> MimeType.WEBP
        MimeTypeDto.IMAGE_HEIC -> MimeType.HEIC
        MimeTypeDto.IMAGE_HEIF -> MimeType.HEIF
        MimeTypeDto.APPLICATION_PDF -> MimeType.PDF
    }

private fun SenderDto.map() =
    when (this) {
        SenderDto.Agent -> Sender.Agent
        SenderDto.Model -> Sender.Model
    }
