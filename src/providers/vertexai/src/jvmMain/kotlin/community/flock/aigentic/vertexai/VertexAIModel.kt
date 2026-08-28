package community.flock.aigentic.vertexai

import com.google.genai.Client
import com.google.genai.types.HttpOptions
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.model.ThinkingLevel
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.vertexai.request.createGenerateConfig
import community.flock.aigentic.vertexai.request.createRequestContents
import community.flock.aigentic.vertexai.response.toModelResponse
import kotlinx.coroutines.future.await

@Suppress("ktlint:standard:class-naming")
sealed class VertexAIModelIdentifier(
    override val stringValue: String,
) : ModelIdentifier {
    /**
     * Default thinking level: medium. Supported levels: LOW, MEDIUM, HIGH (no MINIMAL).
     */
    data object Gemini3_7Flash : VertexAIModelIdentifier("gemini-3.7-flash")

    /**
     * Default thinking level: medium. Supported levels: MINIMAL, LOW, MEDIUM, HIGH.
     */
    data object Gemini3_6Flash : VertexAIModelIdentifier("gemini-3.6-flash")

    /**
     * Default thinking level: medium. Supported levels: MINIMAL, LOW, MEDIUM, HIGH.
     */
    data object Gemini3_5Flash : VertexAIModelIdentifier("gemini-3.5-flash")

    /**
     * Default thinking level: minimal. Supported levels: MINIMAL, LOW, MEDIUM, HIGH.
     */
    data object Gemini3_5FlashLite : VertexAIModelIdentifier("gemini-3.5-flash-lite")

    /**
     * Default thinking level: high. Supported levels: LOW, MEDIUM, HIGH (no MINIMAL).
     */
    data object Gemini3_1ProPreview : VertexAIModelIdentifier("gemini-3.1-pro-preview")

    /**
     * Default thinking level: minimal. Supported levels: MINIMAL, LOW, MEDIUM, HIGH.
     */
    data object Gemini3_1FlashLite : VertexAIModelIdentifier("gemini-3.1-flash-lite")

    /**
     * Default thinking level: high. Supported levels: MINIMAL, LOW, MEDIUM, HIGH.
     */
    data object Gemini3FlashPreview : VertexAIModelIdentifier("gemini-3-flash-preview")

    /**
     * Only supports thinkingBudget, thinkingLevel is not available on this model.
     */
    data object Gemini2_5Pro : VertexAIModelIdentifier("gemini-2.5-pro")

    /**
     * Only supports thinkingBudget, thinkingLevel is not available on this model.
     */
    data object Gemini2_5Flash : VertexAIModelIdentifier("gemini-2.5-flash")

    /**
     * Only supports thinkingBudget, thinkingLevel is not available on this model.
     */
    data object Gemini2_5FlashLite : VertexAIModelIdentifier("gemini-2.5-flash-lite")

    data class Custom(
        val identifier: String,
    ) : VertexAIModelIdentifier(identifier)
}

internal data class ThinkingCapability(
    val supportsThinkingBudget: Boolean,
    val supportedThinkingLevels: Set<ThinkingLevel>?,
)

/**
 * A [VertexAIModelIdentifier.Custom] identifier that starts with "gemini-2" is treated as a Gemini 2.x model
 * (budget only) and one that starts with "gemini-3" as a Gemini 3.x model (level only, all four levels).
 * Any other identifier (tuned endpoints, proxy names, older models) is passed through without validation.
 */
internal fun VertexAIModelIdentifier.thinkingCapability(): ThinkingCapability =
    when (this) {
        VertexAIModelIdentifier.Gemini2_5Pro,
        VertexAIModelIdentifier.Gemini2_5Flash,
        VertexAIModelIdentifier.Gemini2_5FlashLite,
        -> {
            ThinkingCapability(supportsThinkingBudget = true, supportedThinkingLevels = null)
        }

        VertexAIModelIdentifier.Gemini3_7Flash,
        VertexAIModelIdentifier.Gemini3_1ProPreview,
        -> {
            ThinkingCapability(
                supportsThinkingBudget = false,
                supportedThinkingLevels = setOf(ThinkingLevel.LOW, ThinkingLevel.MEDIUM, ThinkingLevel.HIGH),
            )
        }

        VertexAIModelIdentifier.Gemini3_6Flash,
        VertexAIModelIdentifier.Gemini3_5Flash,
        VertexAIModelIdentifier.Gemini3_5FlashLite,
        VertexAIModelIdentifier.Gemini3_1FlashLite,
        VertexAIModelIdentifier.Gemini3FlashPreview,
        -> {
            ThinkingCapability(supportsThinkingBudget = false, supportedThinkingLevels = ThinkingLevel.entries.toSet())
        }

        is VertexAIModelIdentifier.Custom -> {
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

internal fun VertexAIModelIdentifier.supportsThinkingBudget(): Boolean = thinkingCapability().supportsThinkingBudget

internal fun VertexAIModelIdentifier.supportedThinkingLevels(): Set<ThinkingLevel>? = thinkingCapability().supportedThinkingLevels

internal fun validateVertexAIThinkingConfig(
    modelIdentifier: ModelIdentifier,
    generationSettings: GenerationSettings,
) {
    val thinkingConfig = generationSettings.thinkingConfig ?: return
    val vertexAIModelIdentifier = modelIdentifier as? VertexAIModelIdentifier ?: return
    val capability = vertexAIModelIdentifier.thinkingCapability()

    if (thinkingConfig.thinkingBudget != null && !capability.supportsThinkingBudget) {
        aigenticException(
            "thinkingBudget is only supported on Gemini 2.x models, use thinkingLevel() for ${vertexAIModelIdentifier.stringValue}",
        )
    }

    thinkingConfig.thinkingLevel?.let { level ->
        val supportedLevels =
            capability.supportedThinkingLevels
                ?: aigenticException(
                    "thinkingLevel is not supported on Gemini 2.x models, use thinkingBudget() for ${vertexAIModelIdentifier.stringValue}",
                )
        if (level !in supportedLevels) {
            aigenticException(
                "thinkingLevel $level is not supported on ${vertexAIModelIdentifier.stringValue}, supported: ${
                    supportedLevels.joinToString(", ")
                }",
            )
        }
    }
}

@JvmInline
value class Project(
    val value: String,
)

@JvmInline
value class Location(
    val value: String,
)

class VertexAIModel(
    override val modelIdentifier: ModelIdentifier,
    override val generationSettings: GenerationSettings,
    project: Project,
    location: Location,
    requestTimeoutMillis: Long,
) : Model {
    init {
        validateVertexAIThinkingConfig(modelIdentifier, generationSettings)
    }

    private val client: Client = defaultVertexAIClient(project, location, requestTimeoutMillis)

    override suspend fun sendRequest(
        messages: List<Message>,
        tools: List<ToolDescription>,
        structuredOutputParameter: Parameter?,
    ): ModelResponse =
        client.async.models
            .generateContent(
                modelIdentifier.stringValue,
                createRequestContents(messages),
                createGenerateConfig(messages, tools, generationSettings, structuredOutputParameter, modelIdentifier),
            ).await()
            .toModelResponse(structuredOutputParameter != null)

    companion object {
        fun defaultVertexAIClient(
            project: Project,
            location: Location,
            requestTimeoutMillis: Long,
        ): Client =
            Client
                .Builder()
                .vertexAI(true)
                .httpOptions(HttpOptions.builder().timeout(requestTimeoutMillis.toInt()).build())
                .project(project.value)
                .location(location.value)
                .build()
    }
}
