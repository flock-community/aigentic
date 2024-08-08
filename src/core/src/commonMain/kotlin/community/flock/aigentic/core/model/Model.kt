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
    val authentication: Authentication
    val modelIdentifier: ModelIdentifier

    suspend fun sendRequest(
        messages: List<Message>,
        tools: List<ToolDescription>,
    ): ModelResponse
}

data class ModelResponse(
    val message: Message,
    val usage: Usage,
)

data class Usage(
    val inputTokenCount: Int,
    val outputTokenCount: Int,
) {
    companion object {
        val EMPTY = Usage(inputTokenCount = 0, outputTokenCount = 0)
    }
}
