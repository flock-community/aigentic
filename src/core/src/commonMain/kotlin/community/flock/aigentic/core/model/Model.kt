package community.flock.aigentic.core.model

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.tool.ToolDescription

sealed interface Authentication {
    data class APIKey(val key: String) : Authentication
}

interface ModelIdentifier {
    val stringValue: String
}

interface Model {
    val modelIdentifier: ModelIdentifier
    val generationSettings: GenerationSettings

    suspend fun sendRequest(
        messages: List<Message>,
        tools: List<ToolDescription>,
    ): ModelResponse
}

data class ModelResponse(
    val message: Message,
    val usage: Usage,
)

data class GenerationSettings(
    val temperature: Float,
    val topK: Int,
    val topP: Float,
    val thinkingConfig: ThinkingConfig?,
) {
    companion object {
        const val DEFAULT_TEMPERATURE = 0.0f
        const val DEFAULT_TOP_K = 1
        const val DEFAULT_TOP_P = 0.1f

        val DEFAULT =
            GenerationSettings(
                temperature = DEFAULT_TEMPERATURE,
                topK = DEFAULT_TOP_K,
                topP = DEFAULT_TOP_P,
                null,
            )
    }
}

data class ThinkingConfig(
    val thinkingBudget: Int? = null,
)

data class Usage(
    val inputTokenCount: Int,
    val outputTokenCount: Int,
    val thinkingOutputTokenCount: Int = 0,
    val cachedInputTokenCount: Int = 0,
) {
    companion object {
        val EMPTY = Usage(inputTokenCount = 0, outputTokenCount = 0, thinkingOutputTokenCount = 0, cachedInputTokenCount = 0)
    }
}
