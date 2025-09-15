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

fun GenerateContentResponse.toModelResponse(isStructuredOutput: Boolean): ModelResponse {
    promptFeedback?.blockReason?.let {
        aigenticException("Gemini blocked the prompt because of reason: '$it'")
    }

    val candidate =
        candidates?.firstOrNull()
            ?: aigenticException("No candidate found in Gemini response: $this.")

    val usage = usageMetadata?.toUsage() ?: Usage.EMPTY
    val message =
        if (isStructuredOutput) {
            when (val part = candidate.content.parts.first()) {
                is Part.Text -> Message.StructuredOutput(part.text)
                else -> aigenticException("No text candidate found with structured output response: $this.")
            }
        } else {
            candidate.content.toMessages()
        }

    return ModelResponse(message = message, usage = usage)
}

internal fun Content.toMessages(): Message {
    val toolCalls =
        parts.filterIsInstance<Part.FunctionCall>().map {
            ToolCall(
                id = ToolCallId(generateRandomString(20)),
                name = it.functionCall.name,
                arguments = Json.encodeToString(it.functionCall.args),
            )
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
