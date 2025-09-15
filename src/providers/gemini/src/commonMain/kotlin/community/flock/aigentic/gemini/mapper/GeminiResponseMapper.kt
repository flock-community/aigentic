package community.flock.aigentic.gemini.mapper

import community.flock.aigentic.core.exception.aigenticException
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
import kotlinx.serialization.json.Json

fun GenerateContentResponse.toModelResponse(): ModelResponse {
    val candidate = candidates?.firstOrNull()

    return if (promptFeedback?.blockReason != null) {
        aigenticException("Gemini blocked the prompt because of reason: '${promptFeedback.blockReason}'")
    } else if (candidate != null && candidate.content != null) { // Check for null content
        ModelResponse(
            message = candidate.content.toMessages(),
            usage = usageMetadata?.toUsage() ?: Usage.EMPTY,
        )
    } else if (candidate != null && candidate.content == null) {
        aigenticException("Gemini returned candidate without content. Finish reason: ${candidate.finishReason}")
    } else {
        aigenticException("No candidate found in Gemini response: $this.")
    }
}

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
        thinkingOutputTokenCount = thoughtsTokenCount ?: 0,
        cachedInputTokenCount = cachedContentTokenCount ?: 0,
    )
