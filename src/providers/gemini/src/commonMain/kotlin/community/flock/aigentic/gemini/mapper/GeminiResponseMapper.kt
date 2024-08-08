package community.flock.aigentic.gemini.mapper

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.model.Usage
import community.flock.aigentic.gemini.client.model.Content
import community.flock.aigentic.gemini.client.model.GenerateContentResponse
import community.flock.aigentic.gemini.client.model.Part
import community.flock.aigentic.gemini.client.model.UsageMetadata
import generateRandomString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun GenerateContentResponse.toModelResponse(): ModelResponse =
    ModelResponse(
        message = candidates.first().content.toMessages(),
        usage = usageMetadata?.toUsage() ?: Usage.EMPTY,
    )

internal fun Content.toMessages(): Message {
    val toolCalls =
        parts.filterIsInstance<Part.FunctionCall>().map {
            ToolCall(ToolCallId(generateRandomString(20)), it.functionCall.name, Json.encodeToString(it.functionCall.args))
        }
    return Message.ToolCalls(toolCalls)
}

private fun UsageMetadata.toUsage(): Usage =
    Usage(
        inputTokenCount = promptTokenCount ?: 0,
        outputTokenCount = candidatesTokenCount ?: 0,
    )
