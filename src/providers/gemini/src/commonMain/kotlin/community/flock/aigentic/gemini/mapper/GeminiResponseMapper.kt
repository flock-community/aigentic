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

fun GenerateContentResponse.toModelResponse(): ModelResponse =
    when {
        promptFeedback?.blockReason != null ->
            aigenticException("Gemini blocked the prompt because of reason: '${promptFeedback.blockReason}'")
        else -> {
            val candidate =
                candidates?.firstOrNull()
                    ?: aigenticException("No candidate found in Gemini response: $this.")
            val content =
                candidate.content
                    ?: aigenticException("Gemini returned candidate without content. Finish reason: ${candidate.finishReason}")
            ModelResponse(
                message = content.toMessages(),
                usage = usageMetadata?.toUsage() ?: Usage.EMPTY,
            )
        }
    }

internal fun Content.toMessages(): Message =
    Message.ToolCalls(
        parts.filterIsInstance<Part.FunctionCall>().map {
            ToolCall(
                id = ToolCallId(generateRandomString(20)),
                name = it.functionCall.name,
                arguments = Json.encodeToString(it.functionCall.args),
            )
        },
    )

private fun UsageMetadata.toUsage(): Usage =
    Usage(
        inputTokenCount = promptTokenCount ?: 0,
        outputTokenCount = candidatesTokenCount ?: 0,
        thinkingOutputTokenCount = thoughtsTokenCount ?: 0,
        cachedInputTokenCount = cachedContentTokenCount ?: 0,
    )
