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
import community.flock.aigentic.core.model.ThinkingLevel
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
    data object GPT5_5 : OpenAIModelIdentifier("gpt-5.5")

    data object GPT5_5Pro : OpenAIModelIdentifier("gpt-5.5-pro")

    data object GPT5_4 : OpenAIModelIdentifier("gpt-5.4")

    data object GPT5_4Pro : OpenAIModelIdentifier("gpt-5.4-pro")

    data object GPT5_4Mini : OpenAIModelIdentifier("gpt-5.4-mini")

    data object GPT5_4Nano : OpenAIModelIdentifier("gpt-5.4-nano")

    data object GPT5 : OpenAIModelIdentifier("gpt-5")

    data object GPT5Mini : OpenAIModelIdentifier("gpt-5-mini")

    data object GPT5Nano : OpenAIModelIdentifier("gpt-5-nano")

    data object GPT4_1 : OpenAIModelIdentifier("gpt-4.1")

    data object GPT4_1Mini : OpenAIModelIdentifier("gpt-4.1-mini")

    data object GPT4_1Nano : OpenAIModelIdentifier("gpt-4.1-nano")

    data object GPT4O : OpenAIModelIdentifier("gpt-4o")

    data object GPT4OMini : OpenAIModelIdentifier("gpt-4o-mini")

    data object GPT4Turbo : OpenAIModelIdentifier("gpt-4-turbo")

    data object GPT3_5Turbo : OpenAIModelIdentifier("gpt-3.5-turbo")

    data object O3Pro : OpenAIModelIdentifier("o3-pro")

    data object O3 : OpenAIModelIdentifier("o3")

    data object O4Mini : OpenAIModelIdentifier("o4-mini")

    data object O3Mini : OpenAIModelIdentifier("o3-mini")

    data object O1 : OpenAIModelIdentifier("o1")

    data object O1Pro : OpenAIModelIdentifier("o1-pro")

    data object GPT4OMiniSearchPreview : OpenAIModelIdentifier("gpt-4o-mini-search-preview")

    data object GPT4OSearchPreview : OpenAIModelIdentifier("gpt-4o-search-preview")

    data class Custom(
        val identifier: String,
    ) : OpenAIModelIdentifier(identifier)
}

internal fun ModelIdentifier.usesMaxCompletionTokens(): Boolean =
    when (this) {
        is OpenAIModelIdentifier -> requiresMaxCompletionTokens()
        else -> false
    }

internal fun OpenAIModelIdentifier.supportsReasoningEffort(): Boolean =
    when (this) {
        OpenAIModelIdentifier.GPT5_5,
        OpenAIModelIdentifier.GPT5_5Pro,
        OpenAIModelIdentifier.GPT5_4,
        OpenAIModelIdentifier.GPT5_4Pro,
        OpenAIModelIdentifier.GPT5_4Mini,
        OpenAIModelIdentifier.GPT5_4Nano,
        OpenAIModelIdentifier.GPT5,
        OpenAIModelIdentifier.GPT5Mini,
        OpenAIModelIdentifier.GPT5Nano,
        OpenAIModelIdentifier.O3Pro,
        OpenAIModelIdentifier.O3,
        OpenAIModelIdentifier.O4Mini,
        OpenAIModelIdentifier.O3Mini,
        OpenAIModelIdentifier.O1,
        OpenAIModelIdentifier.O1Pro,
        is OpenAIModelIdentifier.Custom,
        -> true

        OpenAIModelIdentifier.GPT4_1,
        OpenAIModelIdentifier.GPT4_1Mini,
        OpenAIModelIdentifier.GPT4_1Nano,
        OpenAIModelIdentifier.GPT4O,
        OpenAIModelIdentifier.GPT4OMini,
        OpenAIModelIdentifier.GPT4Turbo,
        OpenAIModelIdentifier.GPT3_5Turbo,
        OpenAIModelIdentifier.GPT4OMiniSearchPreview,
        OpenAIModelIdentifier.GPT4OSearchPreview,
        -> false
    }

/**
 * MINIMAL is an aigentic-level concept expressing "think as little as possible". Models whose
 * lowest reasoning_effort value isn't "minimal" (the o-series only goes down to LOW, and the
 * gpt-5.4/gpt-5.5 families replaced "minimal" with "none", which this DSL doesn't expose as a
 * ThinkingLevel) fall back to their lowest representable level instead of failing; the base
 * gpt-5 family and custom identifiers pass MINIMAL through unchanged.
 */
internal fun OpenAIModelIdentifier.resolveThinkingLevel(level: ThinkingLevel): ThinkingLevel {
    val minimumLevel = minimumSupportedThinkingLevel()
    return if (level.ordinal < minimumLevel.ordinal) minimumLevel else level
}

private fun OpenAIModelIdentifier.minimumSupportedThinkingLevel(): ThinkingLevel =
    when (this) {
        OpenAIModelIdentifier.O3Pro,
        OpenAIModelIdentifier.O3,
        OpenAIModelIdentifier.O4Mini,
        OpenAIModelIdentifier.O3Mini,
        OpenAIModelIdentifier.O1,
        OpenAIModelIdentifier.O1Pro,
        OpenAIModelIdentifier.GPT5_4,
        OpenAIModelIdentifier.GPT5_4Mini,
        OpenAIModelIdentifier.GPT5_4Nano,
        OpenAIModelIdentifier.GPT5_5,
        -> ThinkingLevel.LOW

        OpenAIModelIdentifier.GPT5_4Pro,
        OpenAIModelIdentifier.GPT5_5Pro,
        -> ThinkingLevel.MEDIUM

        OpenAIModelIdentifier.GPT5,
        OpenAIModelIdentifier.GPT5Mini,
        OpenAIModelIdentifier.GPT5Nano,
        OpenAIModelIdentifier.GPT4_1,
        OpenAIModelIdentifier.GPT4_1Mini,
        OpenAIModelIdentifier.GPT4_1Nano,
        OpenAIModelIdentifier.GPT4O,
        OpenAIModelIdentifier.GPT4OMini,
        OpenAIModelIdentifier.GPT4Turbo,
        OpenAIModelIdentifier.GPT3_5Turbo,
        OpenAIModelIdentifier.GPT4OMiniSearchPreview,
        OpenAIModelIdentifier.GPT4OSearchPreview,
        is OpenAIModelIdentifier.Custom,
        -> ThinkingLevel.MINIMAL
    }

private fun OpenAIModelIdentifier.requiresMaxCompletionTokens(): Boolean =
    when (this) {
        OpenAIModelIdentifier.GPT5_5,
        OpenAIModelIdentifier.GPT5_5Pro,
        OpenAIModelIdentifier.GPT5_4,
        OpenAIModelIdentifier.GPT5_4Pro,
        OpenAIModelIdentifier.GPT5_4Mini,
        OpenAIModelIdentifier.GPT5_4Nano,
        OpenAIModelIdentifier.GPT5,
        OpenAIModelIdentifier.GPT5Mini,
        OpenAIModelIdentifier.GPT5Nano,
        OpenAIModelIdentifier.O3Pro,
        OpenAIModelIdentifier.O3,
        OpenAIModelIdentifier.O4Mini,
        OpenAIModelIdentifier.O3Mini,
        OpenAIModelIdentifier.O1,
        OpenAIModelIdentifier.O1Pro,
        -> true

        OpenAIModelIdentifier.GPT4_1,
        OpenAIModelIdentifier.GPT4_1Mini,
        OpenAIModelIdentifier.GPT4_1Nano,
        OpenAIModelIdentifier.GPT4O,
        OpenAIModelIdentifier.GPT4OMini,
        OpenAIModelIdentifier.GPT4Turbo,
        OpenAIModelIdentifier.GPT3_5Turbo,
        OpenAIModelIdentifier.GPT4OMiniSearchPreview,
        OpenAIModelIdentifier.GPT4OSearchPreview,
        is OpenAIModelIdentifier.Custom,
        -> false
    }

internal fun validateOpenAIThinkingConfig(
    modelIdentifier: ModelIdentifier,
    generationSettings: GenerationSettings,
) {
    val thinkingConfig = generationSettings.thinkingConfig ?: return
    val openAIModelIdentifier = modelIdentifier as? OpenAIModelIdentifier ?: return
    if (thinkingConfig.thinkingBudget != null) {
        aigenticException("thinkingBudget is not supported by OpenAI-compatible models, use thinkingLevel()")
    }
    thinkingConfig.thinkingLevel?.let {
        if (!openAIModelIdentifier.supportsReasoningEffort()) {
            aigenticException(
                "thinkingLevel is not supported on ${openAIModelIdentifier.stringValue}, it is not a reasoning model",
            )
        }
    }
}

class OpenAIModel(
    val authentication: Authentication.APIKey,
    override val modelIdentifier: ModelIdentifier,
    override val generationSettings: GenerationSettings,
    logLevel: LogLevel = LogLevel.NONE,
    apiUrl: OpenAIApiUrl,
) : Model {
    init {
        validateOpenAIThinkingConfig(modelIdentifier, generationSettings)
    }

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
                ).toModelResponse()
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
value class OpenAIApiUrl(
    val value: String,
)
