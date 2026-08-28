package community.flock.aigentic.gemini.model

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
import community.flock.aigentic.gemini.client.GeminiClient
import community.flock.aigentic.gemini.client.config.GeminiApiConfig
import community.flock.aigentic.gemini.client.ratelimit.RateLimitBucket
import community.flock.aigentic.gemini.mapper.createGenerateContentRequest
import community.flock.aigentic.gemini.mapper.toModelResponse

@Suppress("ktlint:standard:class-naming")
sealed class GeminiModelIdentifier(
    override val stringValue: String,
) : ModelIdentifier {
    data object Gemini3_7Flash : GeminiModelIdentifier("gemini-3.7-flash")

    data object Gemini3_6Flash : GeminiModelIdentifier("gemini-3.6-flash")

    data object Gemini3_5Flash : GeminiModelIdentifier("gemini-3.5-flash")

    data object Gemini3_5FlashLite : GeminiModelIdentifier("gemini-3.5-flash-lite")

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

internal data class ThinkingCapability(
    val supportsThinkingBudget: Boolean,
    val supportedThinkingLevels: Set<ThinkingLevel>?,
)

/**
 * A [GeminiModelIdentifier.Custom] identifier that starts with "gemini-2" is treated as a Gemini 2.x model
 * (budget only) and one that starts with "gemini-3" as a Gemini 3.x model (level only, all four levels).
 * Any other identifier (tuned endpoints, proxy names, older models) is passed through without validation.
 */
internal fun GeminiModelIdentifier.thinkingCapability(): ThinkingCapability =
    when (this) {
        GeminiModelIdentifier.Gemini2_5Pro,
        GeminiModelIdentifier.Gemini2_5Flash,
        GeminiModelIdentifier.Gemini2_5FlashLite,
        -> {
            ThinkingCapability(supportsThinkingBudget = true, supportedThinkingLevels = null)
        }

        GeminiModelIdentifier.Gemini3_7Flash,
        GeminiModelIdentifier.Gemini3_1ProPreview,
        -> {
            ThinkingCapability(
                supportsThinkingBudget = false,
                supportedThinkingLevels = setOf(ThinkingLevel.LOW, ThinkingLevel.MEDIUM, ThinkingLevel.HIGH),
            )
        }

        GeminiModelIdentifier.Gemini3_6Flash,
        GeminiModelIdentifier.Gemini3_5Flash,
        GeminiModelIdentifier.Gemini3_5FlashLite,
        GeminiModelIdentifier.Gemini3_1FlashLite,
        GeminiModelIdentifier.Gemini3FlashPreview,
        -> {
            ThinkingCapability(supportsThinkingBudget = false, supportedThinkingLevels = ThinkingLevel.entries.toSet())
        }

        is GeminiModelIdentifier.Custom -> {
            when {
                identifier.startsWith("gemini-2") -> {
                    ThinkingCapability(supportsThinkingBudget = true, supportedThinkingLevels = null)
                }

                identifier.startsWith("gemini-3") -> {
                    ThinkingCapability(supportsThinkingBudget = false, supportedThinkingLevels = ThinkingLevel.entries.toSet())
                }

                else -> {
                    ThinkingCapability(supportsThinkingBudget = true, supportedThinkingLevels = ThinkingLevel.entries.toSet())
                }
            }
        }
    }

internal fun GeminiModelIdentifier.supportsThinkingBudget(): Boolean = thinkingCapability().supportsThinkingBudget

internal fun GeminiModelIdentifier.supportedThinkingLevels(): Set<ThinkingLevel>? = thinkingCapability().supportedThinkingLevels

internal fun validateGeminiThinkingConfig(
    modelIdentifier: GeminiModelIdentifier,
    generationSettings: GenerationSettings,
) {
    val thinkingConfig = generationSettings.thinkingConfig ?: return
    val capability = modelIdentifier.thinkingCapability()

    if (thinkingConfig.thinkingBudget != null && !capability.supportsThinkingBudget) {
        aigenticException(
            "thinkingBudget is only supported on Gemini 2.x models, use thinkingLevel() for ${modelIdentifier.stringValue}",
        )
    }

    thinkingConfig.thinkingLevel?.let { level ->
        val supportedLevels =
            capability.supportedThinkingLevels
                ?: aigenticException(
                    "thinkingLevel is not supported on Gemini 2.x models, use thinkingBudget() for ${modelIdentifier.stringValue}",
                )
        if (level !in supportedLevels) {
            aigenticException(
                "thinkingLevel $level is not supported on ${modelIdentifier.stringValue}, supported: ${
                    supportedLevels.joinToString(", ")
                }",
            )
        }
    }
}

class GeminiModel(
    val authentication: Authentication.APIKey,
    override val modelIdentifier: GeminiModelIdentifier,
    override val generationSettings: GenerationSettings,
    private val logLevel: LogLevel = LogLevel.NONE,
    private val geminiClient: GeminiClient = defaultGeminiClient(authentication, logLevel),
) : Model {
    init {
        validateGeminiThinkingConfig(modelIdentifier, generationSettings)
    }

    override suspend fun sendRequest(
        messages: List<Message>,
        tools: List<ToolDescription>,
        structuredOutputParameter: Parameter?,
    ): ModelResponse =
        geminiClient
            .generateContent(
                request = createGenerateContentRequest(messages, tools, generationSettings, structuredOutputParameter, modelIdentifier),
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
