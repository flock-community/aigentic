package community.flock.aigentic.openai.model

import com.aallam.openai.api.exception.OpenAIException
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.LogLevel
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.openai.mapper.toModelResponse
import community.flock.aigentic.openai.request.createChatCompletionsRequest
import kotlin.jvm.JvmInline
import kotlin.time.Duration.Companion.seconds
import com.aallam.openai.api.logging.LogLevel as OpenAILogLevel

@Suppress("ktlint:standard:class-naming")
sealed class OpenAIModelIdentifier(
    override val stringValue: String,
) : ModelIdentifier {
    data object GPT4O : OpenAIModelIdentifier("gpt-4o")
    data object GPT4OMini : OpenAIModelIdentifier("gpt-4o-mini")
    data object GPT4Turbo : OpenAIModelIdentifier("gpt-4-turbo")
    data object GPT3_5Turbo : OpenAIModelIdentifier("gpt-3.5-turbo")
    data object GPT4_1 : OpenAIModelIdentifier("gpt-4.1")
    data object GPT4_1Mini : OpenAIModelIdentifier("gpt-4.1-mini")
    data object GPT4_1Nano : OpenAIModelIdentifier("gpt-4.1-nano")
    data object GPT4_5Preview : OpenAIModelIdentifier("gpt-4.5-preview")
    data object O1 : OpenAIModelIdentifier("o1")
    data object O1Pro : OpenAIModelIdentifier("o1-pro")
    data object O3 : OpenAIModelIdentifier("o3")
    data object O4Mini : OpenAIModelIdentifier("o4-mini")
    data object O3Mini : OpenAIModelIdentifier("o3-mini")
    data object O1Mini : OpenAIModelIdentifier("o1-mini")
    data object GPT4OMiniSearchPreview : OpenAIModelIdentifier("gpt-4o-mini-search-preview")
    data object GPT4OSearchPreview : OpenAIModelIdentifier("gpt-4o-search-preview")

    data class Custom(val identifier: String) : OpenAIModelIdentifier(identifier)
}

class OpenAIModel(
    val authentication: Authentication.APIKey,
    override val modelIdentifier: ModelIdentifier,
    override val generationSettings: GenerationSettings,
    logLevel: LogLevel = LogLevel.NONE,
    apiUrl: OpenAIApiUrl,
) : Model {
    private val openAI: OpenAI = defaultOpenAI(authentication, apiUrl, logLevel)

    override suspend fun sendRequest(
        messages: List<Message>,
        tools: List<ToolDescription>,
        structuredOutputParameter: Parameter?,
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
            logLevel: LogLevel = LogLevel.NONE,
        ) = OpenAI(
            token = authentication.key,
            logging =
                LoggingConfig(
                    when (logLevel) {
                        LogLevel.NONE -> OpenAILogLevel.None
                        LogLevel.DEBUG -> OpenAILogLevel.All
                    },
                ),
            timeout = Timeout(socket = 60.seconds),
            host = OpenAIHost(apiUrl.value),
        )
    }
}

@JvmInline
value class OpenAIApiUrl(val value: String)
