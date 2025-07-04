package community.flock.aigentic.platform.client

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.core.platform.PlatformClient
import community.flock.aigentic.core.platform.RunSentResult
import community.flock.aigentic.gateway.wirespec.GatewayEndpoint
import community.flock.aigentic.gateway.wirespec.GetRunsEndpoint
import community.flock.aigentic.platform.mapper.toDto
import community.flock.aigentic.platform.mapper.toRun
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
import io.ktor.client.request.accept
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.toByteArray
import io.ktor.util.toMap
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

interface PlatformEndpoints : GatewayEndpoint, GetRunsEndpoint

const val defaultPlatformApiUrl = "https://aigentic-backend-kib53ypjwq-ez.a.run.app/"

class AigenticPlatformClient(
    basicAuth: Authentication.BasicAuth,
    apiUrl: PlatformApiUrl,
    internal val endpoints: PlatformEndpoints = AigenticPlatformEndpoints(basicAuth, apiUrl, null),
) : PlatformClient {
    override suspend fun <I : Any, O : Any> sendRun(
        run: Run<O>,
        agent: Agent<I, O>,
    ): RunSentResult {
        val runDto = run.toDto(agent)
        val request = GatewayEndpoint.RequestApplicationJson(runDto)
        return when (val response = endpoints.gateway(request)) {
            is GatewayEndpoint.Response201Unit -> RunSentResult.Success
            is GatewayEndpoint.Response401Unit -> RunSentResult.Unauthorized
            is GatewayEndpoint.Response400ApplicationJson -> RunSentResult.Error(response.content.body.message)
            is GatewayEndpoint.Response500ApplicationJson ->
                RunSentResult.Error(
                    "${response.content.body.name} - ${response.content.body.description}",
                )
        }
    }

    override suspend fun getRuns(tags: List<RunTag>): List<Pair<RunId, Run<String>>> =
        when (val response = endpoints.getRuns(GetRunsEndpoint.RequestUnit(tags.joinToString(",") { it.value }))) {
            is GetRunsEndpoint.Response200ApplicationJson -> response.content.body
            is GetRunsEndpoint.Response401Unit -> aigenticException("Unauthorized to get runs")
            is GetRunsEndpoint.Response404Unit -> aigenticException("Runs not found")
            is GetRunsEndpoint.Response500ApplicationJson -> aigenticException("Internal server error")
        }.map { RunId(it.runId) to it.toRun<String>() }
}

class AigenticPlatformEndpoints(
    basicAuth: Authentication.BasicAuth,
    apiUrl: PlatformApiUrl,
    engine: HttpClientEngine? = null,
) : PlatformEndpoints {
    private val configuration: HttpClientConfig<*>.() -> Unit = {
        defaultRequest {
            url(apiUrl.value)
            accept(ContentType.Application.Json)
            // TODO wirespec improvement: https://github.com/flock-community/wirespec/issues/254
            header(HttpHeaders.AcceptCharset, "")
            basicAuth(basicAuth.username, basicAuth.password)
        }

        install(ContentNegotiation) {
            json()
        }

        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.NONE
        }

        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }
    }

    private val httpClient = if (engine != null) HttpClient(engine, configuration) else HttpClient(configuration)

    override suspend fun gateway(request: GatewayEndpoint.Request<*>): GatewayEndpoint.Response<*> {
        val responseMapper = GatewayEndpoint.RESPONSE_MAPPER(contentMapper)
        return responseMapper(
            when (request) {
                is GatewayEndpoint.RequestApplicationJson ->
                    httpClient.request(GatewayEndpoint.PATH) {
                        method = GatewayEndpoint.METHOD.toMethod()
                        contentType(request.content.type.toContentType())
                        setBody(request.content.body)
                    }
            }.toWireSpecResponse(),
        )
    }

    override suspend fun getRuns(request: GetRunsEndpoint.Request<*>): GetRunsEndpoint.Response<*> {
        val responseMapper = GetRunsEndpoint.RESPONSE_MAPPER(contentMapper)
        return responseMapper(
            when (request) {
                is GetRunsEndpoint.RequestUnit ->
                    httpClient.request(GetRunsEndpoint.PATH) {
                        method = GetRunsEndpoint.METHOD.toMethod()
                        url {
                            request.query.forEach { (key, values) ->
                                values.forEach { value ->
                                    // TODO does this work for multiple values or does it override?
                                    parameter(key, value)
                                }
                            }
                        }
                    }
            }.toWireSpecResponse(),
        )
    }

    private suspend fun HttpResponse.toWireSpecResponse(): Wirespec.Response<ByteArray> {
        val arr = bodyAsChannel().toByteArray()
        val ktorResponse = this
        return object : Wirespec.Response<ByteArray> {
            override val status: Int = ktorResponse.status.value
            override val headers: Map<String, List<Any?>> = ktorResponse.headers.toMap()
            override val content: Wirespec.Content<ByteArray>? =
                if (ktorResponse.contentType() != null && arr.isNotEmpty()) {
                    Wirespec.Content(
                        ktorResponse.contentType().toString(),
                        arr,
                    )
                } else {
                    null
                }
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
