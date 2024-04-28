package community.flock.aigentic.core.model

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.tool.ToolDescription

sealed interface Authentication {
    data class APIKey(val key: String) : Authentication
}

interface ModelIdentifier

interface Model {
    val authentication: Authentication
    val modelIdentifier: ModelIdentifier

    suspend fun sendRequest(messages: List<Message>, tools: List<ToolDescription>): ModelResponse
}

data class ModelResponse(
    val message: Message
)
