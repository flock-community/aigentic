package community.flock.aigentic.model

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.mapper.toModelResponse
import community.flock.aigentic.request.createChatCompletionsRequest
import kotlin.time.Duration.Companion.seconds

@Suppress("ktlint:standard:class-naming")
sealed class OpenAIModelIdentifier(
    val stringValue: String,
) : ModelIdentifier {
    data object GPT4O : OpenAIModelIdentifier("gpt-4o")
    data object GPT4Turbo : OpenAIModelIdentifier("gpt-4-turbo")
    data object GPT3_5Turbo : OpenAIModelIdentifier("gpt-3.5-turbo")
}

class OpenAIModel(
    override val authentication: Authentication.APIKey,
    override val modelIdentifier: OpenAIModelIdentifier,
    private val openAI: OpenAI = defaultOpenAI(authentication),
) : Model {
    override suspend fun sendRequest(
        messages: List<Message>,
        tools: List<ToolDescription>,
    ): ModelResponse =
        openAI
            .chatCompletion(
                createChatCompletionsRequest(
                    messages = messages,
                    tools = tools,
                    openAIModelIdentifier = modelIdentifier,
                ),
            )
            .toModelResponse()

    companion object {
        fun defaultOpenAI(authentication: Authentication) =
            OpenAI(
                token =
                    (authentication as? Authentication.APIKey).let {
                        it?.key ?: error("OpenAI requires API Key authentication")
                    },
                logging = LoggingConfig(LogLevel.None),
                timeout = Timeout(socket = 60.seconds),
            )
    }
}
