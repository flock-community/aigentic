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
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.LogLevel as KtorLogLevel

internal val geminiJson = Json { ignoreUnknownKeys = true }

class GeminiClient(
    private val config: GeminiApiConfig,
    private val rateLimiter: RateLimiter,
    private val logLevel: LogLevel = NONE,
    engine: HttpClientEngine? = null,
) {
    private val configuration: HttpClientConfig<*>.() -> Unit = {
        install(ContentNegotiation) {
            json(geminiJson)
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level =
                when (logLevel) {
                    NONE -> KtorLogLevel.NONE
                    LogLevel.DEBUG -> KtorLogLevel.ALL
                }
            sanitizeHeader { it == API_KEY_HEADER }
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = config.numberOfRetriesOnServerErrors)
            retryOnException(maxRetries = config.numberOfRetriesOnNetworkErrors, retryOnTimeout = true)
            exponentialDelay()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = config.requestTimeoutMillis
            socketTimeoutMillis = config.socketTimeoutMillis
        }
    }

    private val ktor = if (engine != null) HttpClient(engine, configuration) else HttpClient(configuration)

    suspend fun generateContent(
        request: GenerateContentRequest,
        modelIdentifier: GeminiModelIdentifier,
    ): GenerateContentResponse {
        rateLimiter.consume()

        return try {
            ktor
                .preparePost {
                    url(config.streamGenerateContentUrl(modelIdentifier))
                    header(API_KEY_HEADER, config.apiKey.key)
                    setBody(request)
                    contentType(ContentType.Application.Json)
                }.execute { response ->
                    if (response.status.isSuccess()) {
                        response.bodyAsChannel().readGenerateContentChunks().merge()
                    } else {
                        aigenticException(response.errorMessage())
                    }
                }
        } catch (e: IOException) {
            aigenticException("Gemini request failed: ${e.message ?: e::class.simpleName}", e)
        } catch (e: SerializationException) {
            aigenticException("Could not parse the Gemini response: ${e.message}", e)
        }
    }

    /**
     * The error Gemini answered with. The streaming endpoint sends its JSON error with content type
     * `text/event-stream`, which content negotiation can't decode, so the body is read as text and
     * decoded here; a body that is not Gemini's error JSON is shown as it is.
     */
    private suspend fun HttpResponse.errorMessage(): String {
        val body = bodyAsText()
        val error = runCatching { geminiJson.decodeFromString<ErrorResponse>(body.trim().removePrefix("data:")).error }.getOrNull()
        val details = error?.let { "error code: ${it.status}, message: ${it.message}" } ?: "body: ${body.take(MAX_ERROR_BODY_LENGTH)}"
        return "Received error response from Gemini, http status: $status, $details"
    }

    private fun GeminiApiConfig.streamGenerateContentUrl(modelIdentifier: GeminiModelIdentifier) =
        "$baseUrl/${modelIdentifier.stringValue}:streamGenerateContent?alt=sse"

    private companion object {
        const val API_KEY_HEADER = "x-goog-api-key"
        const val MAX_ERROR_BODY_LENGTH = 500
    }
}
