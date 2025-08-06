package community.flock.aigentic.gemini.model

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.LogLevel
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.gemini.client.GeminiClient
import community.flock.aigentic.gemini.client.config.GeminiApiConfig
import community.flock.aigentic.gemini.client.ratelimit.RateLimitBucket
import community.flock.aigentic.gemini.mapper.createGenerateContentRequest
import community.flock.aigentic.gemini.mapper.toModelResponse

@Suppress("ktlint:standard:class-naming")
sealed class GeminiModelIdentifier(
    override val stringValue: String,
) : ModelIdentifier {
    data object Gemini2_5Flash : GeminiModelIdentifier("gemini-2.5-flash")
    data object Gemini2_5Pro : GeminiModelIdentifier("gemini-2.5")
    data object Gemini2_0Flash : GeminiModelIdentifier("gemini-2.0-flash")
    data object Gemini2_0FlashLite : GeminiModelIdentifier("gemini-2.0-flash-lite")
    data object Gemini1_5Flash : GeminiModelIdentifier("gemini-1.5-flash")
    data object Gemini1_5Flash8b : GeminiModelIdentifier("gemini-1.5-flash-8b")
    data object Gemini1_5Pro : GeminiModelIdentifier("gemini-1.5-pro")

    data class Custom(val identifier: String) : GeminiModelIdentifier(identifier)
}

class GeminiModel(
    val authentication: Authentication.APIKey,
    override val modelIdentifier: GeminiModelIdentifier,
    override val generationSettings: GenerationSettings,
    private val logLevel: LogLevel = LogLevel.NONE,
    private val geminiClient: GeminiClient = defaultGeminiClient(authentication, logLevel),
) : Model {
    override suspend fun sendRequest(
        messages: List<Message>,
        tools: List<ToolDescription>,
    ): ModelResponse {
        val request = createGenerateContentRequest(messages, tools, generationSettings)
        return geminiClient
            .generateContent(request, modelIdentifier)
            .toModelResponse()
    }

    companion object {
        fun defaultGeminiClient(
            apiKeyAuthentication: Authentication.APIKey,
            logLevel: LogLevel = LogLevel.NONE,
            requestsPerMinute: Int = 15,
        ): GeminiClient =
            GeminiClient(
                config = GeminiApiConfig(apiKey = apiKeyAuthentication), // Gebruik defaults
                rateLimiter = RateLimitBucket(requestsPerMinute),
                logLevel = logLevel,
            )

        fun defaultGeminiClient(
            apiKeyAuthentication: Authentication.APIKey,
            logLevel: LogLevel = LogLevel.NONE,
            requestsPerMinute: Int = 15,
            requestTimeoutMillis: Long,
            socketTimeoutMillis: Long,
        ): GeminiClient =
            GeminiClient(
                config =
                    GeminiApiConfig(
                        apiKey = apiKeyAuthentication,
                        requestTimeoutMillis = requestTimeoutMillis,
                        socketTimeoutMillis = socketTimeoutMillis,
                    ),
                rateLimiter = RateLimitBucket(requestsPerMinute),
                logLevel = logLevel,
            )
    }
}
