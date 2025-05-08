package community.flock.aigentic.platform.mapper

import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageType
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.message.ToolResultContent
import community.flock.aigentic.gateway.wirespec.Base64MessageDto
import community.flock.aigentic.gateway.wirespec.FatalResultDto
import community.flock.aigentic.gateway.wirespec.FinishedResultDto
import community.flock.aigentic.gateway.wirespec.MimeTypeDto
import community.flock.aigentic.gateway.wirespec.RunDetailsDto
import community.flock.aigentic.gateway.wirespec.SenderDto
import community.flock.aigentic.gateway.wirespec.StuckResultDto
import community.flock.aigentic.gateway.wirespec.SystemPromptMessageDto
import community.flock.aigentic.gateway.wirespec.TextMessageDto
import community.flock.aigentic.gateway.wirespec.ToolCallsMessageDto
import community.flock.aigentic.gateway.wirespec.ToolResultMessageDto
import community.flock.aigentic.gateway.wirespec.UrlMessageDto
import kotlinx.datetime.Instant

fun RunDetailsDto.toRun() =
    Run(
        startedAt = Instant.parse(startedAt),
        finishedAt = Instant.parse(finishedAt),
        messages =
            messages.map {
                when (it) {
                    is Base64MessageDto -> Message.Base64(it.sender.map(), MessageType.New, it.base64Content, it.mimeType.map())
                    is SystemPromptMessageDto -> Message.SystemPrompt(it.prompt)
                    is TextMessageDto -> Message.Text(it.sender.map(), MessageType.New, it.text)
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

                    is UrlMessageDto -> Message.Url(it.sender.map(), MessageType.New, it.url, it.mimeType.map())
                }
            },
        result =
            when (result) {
                is FatalResultDto -> Result.Fatal(result.message)
                is FinishedResultDto -> Result.Finished(result.description, result.response)
                is StuckResultDto -> Result.Stuck(result.reason)
            },
        modelRequests = listOf(),
    )

fun MimeTypeDto.map() =
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
