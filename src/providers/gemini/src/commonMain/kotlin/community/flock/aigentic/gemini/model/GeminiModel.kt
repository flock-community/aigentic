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
    data object GeminiPro : GeminiModelIdentifier("gemini-pro")
    data object GeminiProVision : GeminiModelIdentifier("gemini-pro-vision")
    data object Gemini1_5ProLatest : GeminiModelIdentifier("gemini-1.5-pro-latest")
    data object Gemini1_5ProLatestStable : GeminiModelIdentifier("gemini-1.5-pro")
    data object Gemini1_5FlashLatest : GeminiModelIdentifier("gemini-1.5-flash-latest")
    data object Gemini1_5FlashLatestStable : GeminiModelIdentifier("gemini-1.5-flash")
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
