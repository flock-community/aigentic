package community.flock.aigentic.gemini.model

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.GenerationSettings
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
    data object Gemini2_5FlashPreview : GeminiModelIdentifier("gemini-2.5-flash-preview-04-17")
    data object Gemini2_5ProPreview : GeminiModelIdentifier("gemini-2.5-pro-preview-03-25")
    data object Gemini2_0Flash : GeminiModelIdentifier("gemini-2.0-flash")
    data object Gemini2_0FlashLite : GeminiModelIdentifier("gemini-2.0-flash-lite")
    data object Gemini1_5Flash : GeminiModelIdentifier("gemini-1.5-flash")
    data object Gemini1_5Flash8b : GeminiModelIdentifier("gemini-1.5-flash-8b")
    data object Gemini1_5Pro : GeminiModelIdentifier("gemini-1.5-pro")

    data class Custom(val identifier: String) : GeminiModelIdentifier(identifier)
}

class GeminiModel(
    override val authentication: Authentication.APIKey,
    override val modelIdentifier: GeminiModelIdentifier,
    override val generationSettings: GenerationSettings,
    private val geminiClient: GeminiClient = defaultGeminiClient(authentication),
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
            requestsPerMinute: Int = 15,
        ): GeminiClient =
            GeminiClient(
                config = GeminiApiConfig(apiKeyAuthentication),
                rateLimiter = RateLimitBucket(requestsPerMinute),
            )
    }
}
