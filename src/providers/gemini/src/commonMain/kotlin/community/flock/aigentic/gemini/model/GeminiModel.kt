package community.flock.aigentic.gemini.model

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.gemini.client.GeminiClient
import community.flock.aigentic.gemini.client.config.GeminiApiConfig
import community.flock.aigentic.gemini.mapper.createGenerateContentRequest
import community.flock.aigentic.gemini.mapper.toModelResponse
import kotlinx.coroutines.delay

@Suppress("ktlint:standard:class-naming")
sealed class GeminiModelIdentifier(
    val stringValue: String,
) : ModelIdentifier {
    data object GeminiPro : GeminiModelIdentifier("gemini-pro")
    data object GeminiProVision : GeminiModelIdentifier("gemini-pro-vision")
    data object Gemini1_5ProLatest : GeminiModelIdentifier("gemini-1.5-pro-latest")
    data object Gemini1_5FlashLatest : GeminiModelIdentifier("gemini-1.5-flash-latest")
}

class GeminiModel(
    override val authentication: Authentication.APIKey,
    override val modelIdentifier: GeminiModelIdentifier,
    private val geminiClient: GeminiClient = defaultGeminiClient(authentication)
) : Model {

    override suspend fun sendRequest(messages: List<Message>, tools: List<ToolDescription>): ModelResponse {
        delay(3_000)
        val request = createGenerateContentRequest(messages, tools)
        return geminiClient
            .generateContent(request, modelIdentifier)
            .toModelResponse()
    }

    companion object {
        fun defaultGeminiClient(authentication: Authentication.APIKey): GeminiClient =
            GeminiClient(GeminiApiConfig(authentication))

    }
}
