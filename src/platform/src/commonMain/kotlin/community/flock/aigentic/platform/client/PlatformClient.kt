package community.flock.aigentic.platform.client

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.AgentRun
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.core.platform.PlatformClient
import community.flock.aigentic.core.platform.RunSentResult
import community.flock.aigentic.gateway.wirespec.endpoint.Gateway
import community.flock.aigentic.gateway.wirespec.endpoint.GetRuns
import community.flock.aigentic.platform.mapper.toDto
import community.flock.aigentic.platform.mapper.toRun
import community.flock.wirespec.kotlin.Wirespec
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
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.toByteArray
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

interface PlatformEndpoints : Gateway.Handler, GetRuns.Handler

const val defaultPlatformApiUrl = "https://aigentic-backend-kib53ypjwq-ez.a.run.app/"

class AigenticPlatformClient(
    basicAuth: Authentication.BasicAuth,
    apiUrl: PlatformApiUrl,
    internal val endpoints: PlatformEndpoints = AigenticPlatformEndpoints(basicAuth, apiUrl, null),
) : PlatformClient {
    override suspend fun <I : Any, O : Any> sendRun(
        run: AgentRun<O>,
        agent: Agent<I, O>,
        outputSerializer: KSerializer<O>,
    ): RunSentResult {
        val runDto = run.toDto(agent, outputSerializer)
        val request = Gateway.Request(body = runDto)
        return when (val response = endpoints.gateway(request)) {
            is Gateway.Response201 -> RunSentResult.Success
            is Gateway.Response401 -> RunSentResult.Unauthorized
            is Gateway.Response400 -> RunSentResult.Error(response.body.message)
            is Gateway.Response500 ->
                RunSentResult.Error(
                    "${response.body.name} - ${response.body.description}",
                )
        }
    }

    override suspend fun getRuns(tags: List<RunTag>): List<Pair<RunId, AgentRun<String>>> =
        when (val response = endpoints.getRuns(GetRuns.Request(tags = tags.joinToString(",") { it.value }))) {
            is GetRuns.Response200 -> response.body
            is GetRuns.Response401 -> aigenticException("Unauthorized to get runs")
            is GetRuns.Response404 -> aigenticException("Runs not found")
            is GetRuns.Response500 -> aigenticException("Internal server error")
        }.map { RunId(it.runId) to it.toRun() }
}

@Suppress("UNCHECKED_CAST")
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

    private val serialization =
        object : Wirespec.Serialization {
            val json =
                Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                }

            override fun <T : Any> serializeBody(
                t: T,
                kType: KType,
            ): ByteArray = json.encodeToString(Json.serializersModule.serializer(kType), t).toByteArray()

            override fun <T : Any> deserializeBody(
                raw: ByteArray,
                kType: KType,
            ): T = json.decodeFromString(Json.serializersModule.serializer(kType), raw.decodeToString()) as T

            override fun <T : Any> serializePath(
                t: T,
                kType: KType,
            ): String = t.toString()

            override fun <T : Any> deserializePath(
                raw: String,
                kType: KType,
            ): T = raw as T

            override fun <T : Any> serializeParam(
                value: T,
                kType: KType,
            ): List<String> = listOf(value.toString())

            override fun <T : Any> deserializeParam(
                values: List<String>,
                kType: KType,
            ): T = values.first() as T
        }

    override suspend fun gateway(request: Gateway.Request): Gateway.Response<*> {
        val edge = Gateway.Handler.client(serialization)
        val rawRequest = edge.to(request)
        val rawResponse = executeRequest(rawRequest)
        return edge.from(rawResponse)
    }

    override suspend fun getRuns(request: GetRuns.Request): GetRuns.Response<*> {
        val edge = GetRuns.Handler.client(serialization)
        val rawRequest = edge.to(request)
        val rawResponse = executeRequest(rawRequest)
        return edge.from(rawResponse)
    }

    private suspend fun executeRequest(rawRequest: Wirespec.RawRequest): Wirespec.RawResponse {
        val response =
            httpClient.request {
                method = HttpMethod.parse(rawRequest.method)
                url {
                    appendPathSegments(rawRequest.path)
                    rawRequest.queries.forEach { (key, values) ->
                        values.forEach { parameters.append(key, it) }
                    }
                }
                rawRequest.headers.forEach { (name, values) ->
                    values.forEach { header(name, it) }
                }
                rawRequest.body?.let {
                    contentType(ContentType.Application.Json)
                    setBody(it)
                }
            }
        return Wirespec.RawResponse(
            statusCode = response.status.value,
            headers = response.headers.entries().associate { (key, values) -> key to values },
            body = response.bodyAsChannel().toByteArray(),
        )
    }
}
