package community.flock.aigentic.platform.mapper

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.gateway.wirespec.ConfigDto
import community.flock.aigentic.gateway.wirespec.FatalResultDto
import community.flock.aigentic.gateway.wirespec.FinishedResultDto
import community.flock.aigentic.gateway.wirespec.ImageBase64MessageDto
import community.flock.aigentic.gateway.wirespec.ImageUrlMessageDto
import community.flock.aigentic.gateway.wirespec.MessageDto
import community.flock.aigentic.gateway.wirespec.MimeTypeDto
import community.flock.aigentic.gateway.wirespec.ModelRequestInfoDto
import community.flock.aigentic.gateway.wirespec.RunDto
import community.flock.aigentic.gateway.wirespec.SenderDto
import community.flock.aigentic.gateway.wirespec.StuckResultDto
import community.flock.aigentic.gateway.wirespec.SystemPromptMessageDto
import community.flock.aigentic.gateway.wirespec.TaskDto
import community.flock.aigentic.gateway.wirespec.TextMessageDto
import community.flock.aigentic.gateway.wirespec.ToolCallDto
import community.flock.aigentic.gateway.wirespec.ToolCallsMessageDto
import community.flock.aigentic.gateway.wirespec.ToolDto
import community.flock.aigentic.gateway.wirespec.ToolResultMessageDto
import community.flock.aigentic.providers.jsonschema.emitPropertiesAndRequired
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun Run.toDto(agent: Agent) =
    RunDto(
        startedAt = startedAt.toString(),
        finishedAt = finishedAt.toString(),
        config =
            ConfigDto(
                task =
                    TaskDto(
                        description = agent.task.description,
                        instructions = agent.task.instructions.map { it.text },
                    ),
                modelIdentifier = agent.model.modelIdentifier.stringValue,
                systemPrompt = messages.filterIsInstance<Message.SystemPrompt>().first().prompt,
                tools =
                    agent.tools.map { (name, tool) ->
                        ToolDto(
                            name = name.value,
                            description = tool.description,
                            parameters =
                                Json.encodeToString(
                                    buildJsonObject {
                                        put("type", "object")
                                        emitPropertiesAndRequired(tool.parameters)
                                    },
                                ),
                        )
                    },
            ),
        messages = messages.map { it.toDto() },
        modelRequests = modelRequests.map { it.toDto() },
        result = result.toDto(),
    )

private fun ModelRequestInfo.toDto(): ModelRequestInfoDto =
    ModelRequestInfoDto(
        startedAt = startedAt.toString(),
        finishedAt = finishedAt.toString(),
        inputTokenCount = inputTokenCount.toLong(),
        outputTokenCount = outputTokenCount.toLong(),
    )

private fun Sender.toDto(): SenderDto =
    when (this) {
        is Sender.Agent -> SenderDto.Agent
        is Sender.Model -> SenderDto.Model
    }

private fun Message.toDto(): MessageDto =
    when (this) {
        is Message.ImageBase64 ->
            ImageBase64MessageDto(
                sender = sender.toDto(),
                base64Content = base64Content,
                mimeType = mimeType.toDto(),
            )

        is Message.ImageUrl ->
            ImageUrlMessageDto(
                sender = sender.toDto(),
                url = url,
                mimeType = mimeType.toDto(),
            )

        is Message.SystemPrompt ->
            SystemPromptMessageDto(
                sender = sender.toDto(),
                prompt = prompt,
            )

        is Message.Text ->
            TextMessageDto(
                sender = sender.toDto(),
                text = text,
            )

        is Message.ToolCalls ->
            ToolCallsMessageDto(
                sender = sender.toDto(),
                toolCalls = toolCalls.map { it.toDto() },
            )

        is Message.ToolResult ->
            ToolResultMessageDto(
                sender = sender.toDto(),
                toolCallId = toolCallId.id,
                response = response.result,
                toolName = toolName,
            )
    }

private fun ToolCall.toDto(): ToolCallDto =
    ToolCallDto(
        id = id.id,
        name = name,
        arguments = arguments,
    )

private fun MimeType.toDto(): MimeTypeDto = MimeTypeDto.valueOf(this.value)

private fun Result.toDto() =
    when (this) {
        is Result.Fatal ->
            FatalResultDto(
                message = message,
            )

        is Result.Finished ->
            FinishedResultDto(
                description = description,
                response = response,
            )

        is Result.Stuck ->
            StuckResultDto(
                reason = reason,
            )
    }
