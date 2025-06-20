package community.flock.aigentic.platform.mapper

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.PrimitiveValue
import community.flock.aigentic.gateway.wirespec.Base64MessageDto
import community.flock.aigentic.gateway.wirespec.ConfigDto
import community.flock.aigentic.gateway.wirespec.FatalResultDto
import community.flock.aigentic.gateway.wirespec.FinishedResultDto
import community.flock.aigentic.gateway.wirespec.MessageDto
import community.flock.aigentic.gateway.wirespec.MimeTypeDto
import community.flock.aigentic.gateway.wirespec.ModelRequestInfoDto
import community.flock.aigentic.gateway.wirespec.ParameterArrayDto
import community.flock.aigentic.gateway.wirespec.ParameterDto
import community.flock.aigentic.gateway.wirespec.ParameterEnumDto
import community.flock.aigentic.gateway.wirespec.ParameterObjectDto
import community.flock.aigentic.gateway.wirespec.ParameterPrimitiveDto
import community.flock.aigentic.gateway.wirespec.ParameterTypeDto
import community.flock.aigentic.gateway.wirespec.PrimitiveValueBooleanDto
import community.flock.aigentic.gateway.wirespec.PrimitiveValueDto
import community.flock.aigentic.gateway.wirespec.PrimitiveValueIntegerDto
import community.flock.aigentic.gateway.wirespec.PrimitiveValueNumberDto
import community.flock.aigentic.gateway.wirespec.PrimitiveValueStringDto
import community.flock.aigentic.gateway.wirespec.PrimitiveValueTypeDto
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
import community.flock.aigentic.gateway.wirespec.UrlMessageDto

fun <I, O> Run.toDto(agent: Agent<I, O>) =
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
                exampleRunIds = exampleRunIds.map { it.value },
                tools =
                    agent.tools.map { (name, tool) ->
                        ToolDto(
                            name = name.value,
                            description = tool.description,
                            parameters = tool.parameters.map { it.toDto() },
                        )
                    },
            ),
        messages = messages.mapNotNull { it.toDto() },
        modelRequests = modelRequests.map { it.toDto() },
        result = result.toDto(),
    )

private fun Parameter.toDto(): ParameterDto =
    when (this) {
        is Parameter.Primitive ->
            ParameterPrimitiveDto(
                name = name,
                description = description,
                isRequired = isRequired,
                paramType = type.toDto(),
            )

        is Parameter.Complex.Object ->
            ParameterObjectDto(
                name = name,
                description = description,
                isRequired = isRequired,
                paramType = type.toDto(),
                parameters = parameters.map { it.toDto() },
            )

        is Parameter.Complex.Array ->
            ParameterArrayDto(
                name = name,
                description = description,
                isRequired = isRequired,
                paramType = type.toDto(),
                itemDefinition = itemDefinition.toDto(),
            )

        is Parameter.Complex.Enum ->

            ParameterEnumDto(
                name = name,
                description = description,
                isRequired = isRequired,
                paramType = type.toDto(),
                values =
                    values.map { value ->
                        val parameterType = determineType(value)
                        this.createEnumValues(parameterType, value)
                    },
                valueType = valueType.toDto(),
            )
    }

private fun determineType(value: PrimitiveValue<*>): PrimitiveValueTypeDto =
    when (value) {
        is PrimitiveValue.String -> PrimitiveValueTypeDto.STRING
        is PrimitiveValue.Number -> PrimitiveValueTypeDto.NUMBER
        is PrimitiveValue.Boolean -> PrimitiveValueTypeDto.BOOLEAN
        is PrimitiveValue.Integer -> PrimitiveValueTypeDto.INTEGER
    }

private fun Parameter.Complex.Enum.createEnumValues(
    parameterType: PrimitiveValueTypeDto,
    value: PrimitiveValue<*>,
): PrimitiveValueDto =
    when (parameterType) {
        PrimitiveValueTypeDto.STRING -> PrimitiveValueStringDto(value.toString())
        PrimitiveValueTypeDto.INTEGER -> PrimitiveValueIntegerDto(value.toString().toLong())
        PrimitiveValueTypeDto.BOOLEAN -> PrimitiveValueBooleanDto(value.toString().toBoolean())
        PrimitiveValueTypeDto.NUMBER -> PrimitiveValueNumberDto(value.toString().toDouble())
    }

private fun ParameterType.toDto() =
    when (this) {
        is Primitive.String -> ParameterTypeDto.STRING
        is Primitive.Boolean -> ParameterTypeDto.BOOLEAN
        is Primitive.Number -> ParameterTypeDto.NUMBER
        is Primitive.Integer -> ParameterTypeDto.INTEGER
        is ParameterType.Complex.Object -> ParameterTypeDto.OBJECT
        is ParameterType.Complex.Array -> ParameterTypeDto.ARRAY
        is ParameterType.Complex.Enum -> ParameterTypeDto.ENUM
    }

private fun ModelRequestInfo.toDto(): ModelRequestInfoDto =
    ModelRequestInfoDto(
        startedAt = startedAt.toString(),
        finishedAt = finishedAt.toString(),
        inputTokenCount = inputTokenCount.toLong(),
        outputTokenCount = outputTokenCount.toLong(),
        thinkingOutputTokenCount = thinkingOutputTokenCount.toLong(),
        cachedInputTokenCount = cachedInputTokenCount.toLong(),
    )

private fun Sender.toDto(): SenderDto =
    when (this) {
        is Sender.Agent -> SenderDto.Agent
        is Sender.Model -> SenderDto.Model
    }

private fun Message.toDto(): MessageDto? =
    when (this) {
        is Message.Base64 ->
            Base64MessageDto(
                createdAt = createdAt.toString(),
                sender = sender.toDto(),
                base64Content = base64Content,
                mimeType = mimeType.toDto(),
            )

        is Message.Url ->
            UrlMessageDto(
                createdAt = createdAt.toString(),
                sender = sender.toDto(),
                url = url,
                mimeType = mimeType.toDto(),
            )

        is Message.SystemPrompt ->
            SystemPromptMessageDto(
                createdAt = createdAt.toString(),
                sender = sender.toDto(),
                prompt = prompt,
            )

        is Message.Text ->
            TextMessageDto(
                createdAt = createdAt.toString(),
                sender = sender.toDto(),
                text = text,
            )

        is Message.ToolCalls ->
            ToolCallsMessageDto(
                createdAt = createdAt.toString(),
                sender = sender.toDto(),
                toolCalls = toolCalls.map { it.toDto() },
            )

        is Message.ToolResult ->
            ToolResultMessageDto(
                createdAt = createdAt.toString(),
                sender = sender.toDto(),
                toolCallId = toolCallId.id,
                response = response.result,
                toolName = toolName,
            )

        is Message.ExampleToolMessage -> null
    }

private fun ToolCall.toDto(): ToolCallDto =
    ToolCallDto(
        id = id.id,
        name = name,
        arguments = arguments,
    )

private fun MimeType.toDto(): MimeTypeDto =
    when (this) {
        MimeType.JPEG -> MimeTypeDto.IMAGE_JPEG
        MimeType.PNG -> MimeTypeDto.IMAGE_PNG
        MimeType.WEBP -> MimeTypeDto.IMAGE_WEBP
        MimeType.HEIC -> MimeTypeDto.IMAGE_HEIC
        MimeType.HEIF -> MimeTypeDto.IMAGE_HEIF
        MimeType.PDF -> MimeTypeDto.APPLICATION_PDF
    }

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
