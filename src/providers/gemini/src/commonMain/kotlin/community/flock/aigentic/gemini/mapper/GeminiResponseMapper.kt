package community.flock.aigentic.gemini.mapper

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.gemini.client.model.Content
import community.flock.aigentic.gemini.client.model.GenerateContentResponse
import community.flock.aigentic.gemini.client.model.Part
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun GenerateContentResponse.toModelResponse(): ModelResponse = ModelResponse(
    message = candidates.first().content.toMessages()
)

internal fun Content.toMessages(): Message  {

    val toolCalls = parts.filterIsInstance<Part.FunctionCall>().map { ToolCall(ToolCallId(""), it.functionCall.name, Json.encodeToString(it.functionCall.args)) }

    if(toolCalls.size != parts.size) {
        error("Help")
    }

    return Message.ToolCalls(toolCalls)
}
