package community.flock.aigentic.gemini.client

import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.model.LogLevel
import community.flock.aigentic.core.model.LogLevel.NONE
import community.flock.aigentic.gemini.client.config.GeminiApiConfig
import community.flock.aigentic.gemini.client.model.ErrorResponse
import community.flock.aigentic.gemini.client.model.GenerateContentRequest
import community.flock.aigentic.gemini.client.model.GenerateContentResponse
import community.flock.aigentic.gemini.client.ratelimit.RateLimiter
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.LogLevel as KtorLogLevel

class GeminiClient(
    private val config: GeminiApiConfig,
    private val rateLimiter: RateLimiter,
    private val logLevel: LogLevel = NONE,
    engine: HttpClientEngine? = null,
) {
    private val configuration: HttpClientConfig<*>.() -> Unit = {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level =
                when (logLevel) {
                    NONE -> KtorLogLevel.NONE
                    LogLevel.DEBUG -> KtorLogLevel.ALL
                }
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = config.numberOfRetriesOnServerErrors)
            exponentialDelay()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
        }
    }

    private val ktor = if (engine != null) HttpClient(engine, configuration) else HttpClient(configuration)

    suspend fun generateContent(
        request: GenerateContentRequest,
        modelIdentifier: GeminiModelIdentifier,
    ): GenerateContentResponse {
        rateLimiter.consume()

        val response =
            ktor.post {
                url(config.generateContentUrl(modelIdentifier))
                setBody(request)
                contentType(ContentType.Application.Json)
            }

        return if (response.status.isSuccess()) {
            response.body()
        } else {
            val errorDetails = response.body<ErrorResponse>().error
            aigenticException(
                "Received error response from Gemini, " +
                    "http status: ${response.status}, " +
                    "error code: ${errorDetails.status}, " +
                    "message: ${errorDetails.message}",
            )
        }
    }

    private fun GeminiApiConfig.generateContentUrl(modelIdentifier: GeminiModelIdentifier) =
        "$baseUrl/${modelIdentifier.stringValue}:generateContent?key=${apiKey.key}"
}
