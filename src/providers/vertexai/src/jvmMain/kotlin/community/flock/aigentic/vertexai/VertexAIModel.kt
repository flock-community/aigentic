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
    data object Gemini3_7Flash : VertexAIModelIdentifier("gemini-3.7-flash")

    data object Gemini3_6Flash : VertexAIModelIdentifier("gemini-3.6-flash")

    data object Gemini3_5Flash : VertexAIModelIdentifier("gemini-3.5-flash")

    data object Gemini3_5FlashLite : VertexAIModelIdentifier("gemini-3.5-flash-lite")

    data object Gemini3_1ProPreview : VertexAIModelIdentifier("gemini-3.1-pro-preview")

    data object Gemini3_1FlashLite : VertexAIModelIdentifier("gemini-3.1-flash-lite")

    data object Gemini3FlashPreview : VertexAIModelIdentifier("gemini-3-flash-preview")

    data object Gemini2_5Pro : VertexAIModelIdentifier("gemini-2.5-pro")

    data object Gemini2_5Flash : VertexAIModelIdentifier("gemini-2.5-flash")

    data object Gemini2_5FlashLite : VertexAIModelIdentifier("gemini-2.5-flash-lite")

    data class Custom(
        val identifier: String,
    ) : VertexAIModelIdentifier(identifier)
}

internal data class ModelCapability(
    val supportsThinkingBudget: Boolean,
    val supportedThinkingLevels: Set<ThinkingLevel>?,
    val supportsSampling: Boolean,
)

private val vertexAIMajorVersionRegex = Regex("(?:^|/)gemini-(\\d+)")

internal fun VertexAIModelIdentifier.capability(): ModelCapability =
    when (this) {
        VertexAIModelIdentifier.Gemini2_5Pro,
        VertexAIModelIdentifier.Gemini2_5Flash,
        VertexAIModelIdentifier.Gemini2_5FlashLite,
        -> {
            ModelCapability(supportsThinkingBudget = true, supportedThinkingLevels = null, supportsSampling = true)
        }

        VertexAIModelIdentifier.Gemini3_7Flash,
        VertexAIModelIdentifier.Gemini3_1ProPreview,
        -> {
            ModelCapability(
                supportsThinkingBudget = false,
                supportedThinkingLevels = setOf(ThinkingLevel.LOW, ThinkingLevel.MEDIUM, ThinkingLevel.HIGH),
                supportsSampling = false,
            )
        }

        VertexAIModelIdentifier.Gemini3_6Flash,
        VertexAIModelIdentifier.Gemini3_5Flash,
        VertexAIModelIdentifier.Gemini3_5FlashLite,
        VertexAIModelIdentifier.Gemini3_1FlashLite,
        VertexAIModelIdentifier.Gemini3FlashPreview,
        -> {
            ModelCapability(
                supportsThinkingBudget = false,
                supportedThinkingLevels = ThinkingLevel.entries.toSet(),
                supportsSampling = false,
            )
        }

        is VertexAIModelIdentifier.Custom -> {
            val majorVersion =
                vertexAIMajorVersionRegex
                    .find(identifier)
                    ?.groupValues
                    ?.get(1)
                    ?.toIntOrNull()
            when {
                majorVersion == null -> {
                    ModelCapability(
                        supportsThinkingBudget = true,
                        supportedThinkingLevels = ThinkingLevel.entries.toSet(),
                        supportsSampling = true,
                    )
                }

                majorVersion <= 2 -> {
                    ModelCapability(supportsThinkingBudget = true, supportedThinkingLevels = null, supportsSampling = true)
                }

                else -> {
                    ModelCapability(
                        supportsThinkingBudget = false,
                        supportedThinkingLevels = ThinkingLevel.entries.toSet(),
                        supportsSampling = false,
                    )
                }
            }
        }
    }

internal fun VertexAIModelIdentifier.supportsSampling(): Boolean = capability().supportsSampling

/**
 * MINIMAL is an aigentic-level concept expressing "think as little as possible". Models that
 * don't have a MINIMAL level of their own (gemini-3.7-flash, gemini-3.1-pro-preview) fall back to
 * their lowest supported level, LOW, instead of failing.
 */
internal fun VertexAIModelIdentifier.resolveThinkingLevel(level: ThinkingLevel): ThinkingLevel {
    val supportedLevels = capability().supportedThinkingLevels
    return if (level == ThinkingLevel.MINIMAL && supportedLevels != null && ThinkingLevel.MINIMAL !in supportedLevels) {
        ThinkingLevel.LOW
    } else {
        level
    }
}

internal fun validateVertexAIThinkingConfig(
    modelIdentifier: ModelIdentifier,
    generationSettings: GenerationSettings,
) {
    val thinkingConfig = generationSettings.thinkingConfig ?: return
    val vertexAIModelIdentifier = modelIdentifier as? VertexAIModelIdentifier ?: return
    val capability = vertexAIModelIdentifier.capability()

    if (thinkingConfig.thinkingBudget != null && !capability.supportsThinkingBudget) {
        aigenticException(
            "thinkingBudget is only supported on Gemini 2.x models, use thinkingLevel() for ${vertexAIModelIdentifier.stringValue}",
        )
    }

    thinkingConfig.thinkingLevel?.let {
        capability.supportedThinkingLevels
            ?: aigenticException(
                "thinkingLevel is not supported on Gemini 2.x models, use thinkingBudget() for ${vertexAIModelIdentifier.stringValue}",
            )
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
