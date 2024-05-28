package community.flock.aigentic.gemini.client

import community.flock.aigentic.gemini.client.config.GeminiApiConfig
import community.flock.aigentic.gemini.client.model.ErrorResponse
import community.flock.aigentic.gemini.client.model.GenerateContentRequest
import community.flock.aigentic.gemini.client.model.GenerateContentResponse
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

class GeminiClient(
    private val config: GeminiApiConfig,
    private val settings: GeminiSettings,
    engine: HttpClientEngine? = null
) {

    private val configuration: HttpClientConfig<*>.() -> Unit = {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 5)
            exponentialDelay()
        }
    }

    private val ktor = if (engine != null) HttpClient(engine, configuration) else HttpClient(configuration)

    suspend fun generateContent(request: GenerateContentRequest, modelIdentifier: GeminiModelIdentifier): GenerateContentResponse {

        delayRequestsPerMinutes()

        val response = ktor.post {
            url(config.generateContentUrl(modelIdentifier))
            setBody(request)
            contentType(ContentType.Application.Json)
        }

        return if(response.status.isSuccess()) {
            response.body()
        } else {
            val errorDetails = response.body<ErrorResponse>().error
            error("Received error code: ${errorDetails.status} message: ${errorDetails.message} status: ${errorDetails.status}")
        }
    }

    suspend fun delayRequestsPerMinutes() {
        delay((60 / settings.requestsPerMinute).toLong())
    }

    private fun GeminiApiConfig.generateContentUrl(modelIdentifier: GeminiModelIdentifier) =
        "$baseUrl/${modelIdentifier.stringValue}:generateContent?key=${apiKey.key}"

}

data class GeminiSettings(
    val requestsPerMinute: Int = 5
)
