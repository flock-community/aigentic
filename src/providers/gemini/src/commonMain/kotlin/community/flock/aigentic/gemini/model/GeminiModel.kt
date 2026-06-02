package community.flock.aigentic.gemini.model

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.LogLevel
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.Parameter
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
    data object Gemini3_5Flash : GeminiModelIdentifier("gemini-3.5-flash")

    data object Gemini3_1ProPreview : GeminiModelIdentifier("gemini-3.1-pro-preview")

    data object Gemini3_1FlashLite : GeminiModelIdentifier("gemini-3.1-flash-lite")

    data object Gemini3FlashPreview : GeminiModelIdentifier("gemini-3-flash-preview")

    data object Gemini2_5Pro : GeminiModelIdentifier("gemini-2.5-pro")

    data object Gemini2_5Flash : GeminiModelIdentifier("gemini-2.5-flash")

    data object Gemini2_5FlashLite : GeminiModelIdentifier("gemini-2.5-flash-lite")

    data class Custom(
        val identifier: String,
    ) : GeminiModelIdentifier(identifier)
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
        structuredOutputParameter: Parameter?,
    ): ModelResponse =
        geminiClient
            .generateContent(
                request = createGenerateContentRequest(messages, tools, generationSettings, structuredOutputParameter),
                modelIdentifier = modelIdentifier,
            ).toModelResponse(structuredOutputParameter != null)

    companion object {
        fun defaultGeminiClient(
            apiKeyAuthentication: Authentication.APIKey,
            logLevel: LogLevel = LogLevel.NONE,
            requestsPerMinute: Int = 50,
        ): GeminiClient =
            GeminiClient(
                config = GeminiApiConfig(apiKey = apiKeyAuthentication),
                rateLimiter = RateLimitBucket(requestsPerMinute),
                logLevel = logLevel,
            )

        fun defaultGeminiClient(
            apiKeyAuthentication: Authentication.APIKey,
            logLevel: LogLevel = LogLevel.NONE,
            requestsPerMinute: Int = 50,
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
