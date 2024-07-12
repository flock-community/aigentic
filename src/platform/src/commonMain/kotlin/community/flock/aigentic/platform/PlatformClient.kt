package community.flock.aigentic.platform

import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.wirespec.ConfigDto
import community.flock.aigentic.wirespec.FatalResultDto
import community.flock.aigentic.wirespec.FinishedResultDto
import community.flock.aigentic.wirespec.GatewayEndpoint
import community.flock.aigentic.wirespec.ImageBase64MessageDto
import community.flock.aigentic.wirespec.ImageUrlMessageDto
import community.flock.aigentic.wirespec.MessageDto
import community.flock.aigentic.wirespec.MimeTypeDto
import community.flock.aigentic.wirespec.RunDto
import community.flock.aigentic.wirespec.SenderDto
import community.flock.aigentic.wirespec.StuckResultDto
import community.flock.aigentic.wirespec.SystemPromptMessageDto
import community.flock.aigentic.wirespec.TaskDto
import community.flock.aigentic.wirespec.TextMessageDto
import community.flock.aigentic.wirespec.ToolCallDto
import community.flock.aigentic.wirespec.ToolCallsMessageDto
import community.flock.aigentic.wirespec.ToolResultMessageDto
import community.flock.wirespec.Wirespec
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.toByteArray
import io.ktor.util.toMap
import io.ktor.utils.io.core.toByteArray
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

class PlatformGatewayClient(engine: HttpClientEngine) : GatewayEndpoint {
    private val httpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json()
            }
        }

    override suspend fun gateway(request: GatewayEndpoint.Request<*>): GatewayEndpoint.Response<*> {
        val responseMapper = GatewayEndpoint.RESPONSE_MAPPER(contentMapper)
        return when (request) {
            is GatewayEndpoint.RequestApplicationJson -> {
                httpClient.request(GatewayEndpoint.PATH) {
                    method = GatewayEndpoint.METHOD.toMethod()
                    contentType(request.content.type.toContentType())
                    setBody(request.content.body)
                }
            }
        }.let { r ->
            val arr = r.bodyAsChannel().toByteArray()
            val res =
                object : Wirespec.Response<ByteArray> {
                    override val status: Int
                        get() = r.status.value
                    override val headers: Map<String, List<Any?>>
                        get() = r.headers.toMap()
                    override val content: Wirespec.Content<ByteArray>?
                        get() =
                            r.contentType()?.let {
                                Wirespec.Content(
                                    it.toString(),
                                    arr,
                                )
                            }
                }
            responseMapper(res)
        }
    }

    private val contentMapper =
        object : Wirespec.ContentMapper<ByteArray> {
            val json =
                Json {
                    prettyPrint = true
                }

            override fun <T> read(
                content: Wirespec.Content<ByteArray>,
                valueType: KType,
            ): Wirespec.Content<T> {
                return Wirespec.Content(
                    content.type,
                    json.decodeFromString(Json.serializersModule.serializer(valueType), content.body.decodeToString()) as T,
                )
            }

            override fun <T> write(
                content: Wirespec.Content<T>,
                valueType: KType,
            ): Wirespec.Content<ByteArray> =
                Wirespec.Content(
                    content.type,
                    json.encodeToString(Json.serializersModule.serializer(valueType), content.body).toByteArray(),
                )
        }
}

private fun String.toContentType() = ContentType.parse(this)

private fun String.toMethod() = HttpMethod.parse(this)

fun Run.toDto() =
    RunDto(
        startedAt = startedAt.formatDateTime(),
        finishedAt = finishedAt.formatDateTime(),
        config =
            ConfigDto(
                task =
                    TaskDto(
                        description = "",
                        instructions = emptyList(),
                    ),
                modelIdentifier = "",
                systemPrompt = "",
                tools = emptyList(),
            ),
        messages = messages.map { it.toDto() },
        modelRequests = emptyList(),
        result = result.toDto(),
    )

private fun Sender.toDto(): SenderDto =
    when (this) {
        is Sender.Aigentic -> SenderDto.Aigentic
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

private fun Instant.formatDateTime() = LocalDateTime.Formats.ISO.format(toLocalDateTime(TimeZone.UTC))
