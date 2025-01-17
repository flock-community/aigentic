package community.flock.aigentic.openai.model

import com.aallam.openai.api.exception.OpenAIException
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.openai.mapper.toModelResponse
import community.flock.aigentic.openai.request.createChatCompletionsRequest
import kotlin.jvm.JvmInline
import kotlin.time.Duration.Companion.seconds

@Suppress("ktlint:standard:class-naming")
sealed class OpenAIModelIdentifier(
    override val stringValue: String,
) : ModelIdentifier {
    data object GPTO1 : OpenAIModelIdentifier("o1")
    data object GPTO1Mini : OpenAIModelIdentifier("o1-mini")
    data object GPTO1Preview : OpenAIModelIdentifier("o1-preview")
    data object GPT4O : OpenAIModelIdentifier("gpt-4o")
    data object GPT4OLatest : OpenAIModelIdentifier("chatgpt-4o-latest")
    data object GPT4OMini : OpenAIModelIdentifier("gpt-4o-mini")
    data object GPT4Turbo : OpenAIModelIdentifier("gpt-4-turbo")
    data object GPT3_5Turbo : OpenAIModelIdentifier("gpt-3.5-turbo")

    data class GPT_Custom_Identifier(val modelIdentifier: String) : OpenAIModelIdentifier(modelIdentifier)
}

class OpenAIModel(
    override val authentication: Authentication.APIKey,
    override val modelIdentifier: ModelIdentifier,
    override val generationSettings: GenerationSettings,
    apiUrl: OpenAIApiUrl,
) : Model {
    private val openAI: OpenAI = defaultOpenAI(authentication, apiUrl)

    override suspend fun sendRequest(
        messages: List<Message>,
        tools: List<ToolDescription>,
    ): ModelResponse =
        try {
            openAI
                .chatCompletion(
                    createChatCompletionsRequest(
                        messages = messages,
                        tools = tools,
                        openAIModelIdentifier = modelIdentifier,
                        generationSettings = generationSettings,
                    ),
                )
                .toModelResponse()
        } catch (e: OpenAIException) {
            aigenticException(e.message ?: "OpenAI error", e)
        }

    companion object {
        fun defaultOpenAI(
            authentication: Authentication.APIKey,
            apiUrl: OpenAIApiUrl,
        ) = OpenAI(
            token = authentication.key,
            logging = LoggingConfig(LogLevel.None),
            timeout = Timeout(socket = 60.seconds),
            host = OpenAIHost(apiUrl.value),
        )
    }
}

@JvmInline
value class OpenAIApiUrl(val value: String)
