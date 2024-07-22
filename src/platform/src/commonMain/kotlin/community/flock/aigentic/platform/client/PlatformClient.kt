package community.flock.aigentic.platform.client

import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.gateway.wirespec.ConfigDto
import community.flock.aigentic.gateway.wirespec.FatalResultDto
import community.flock.aigentic.gateway.wirespec.FinishedResultDto
import community.flock.aigentic.gateway.wirespec.GatewayEndpoint
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
import community.flock.aigentic.gateway.wirespec.ToolResultMessageDto
import community.flock.wirespec.Wirespec
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.basicAuth
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

class PlatformGatewayClient(
    basicAuth: Authentication.BasicAuth,
    apiUrl: PlatformApiUrl,
    engine: HttpClientEngine? = null) : GatewayEndpoint {

    private val configuration: HttpClientConfig<*>.() -> Unit = {
        defaultRequest {
            url(apiUrl.value)
            basicAuth(basicAuth.username, basicAuth.password)
        }

        install(ContentNegotiation) {
            json()
        }

        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }

        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }
    }

    private val httpClient = if (engine != null) HttpClient(engine, configuration) else HttpClient(configuration)

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
