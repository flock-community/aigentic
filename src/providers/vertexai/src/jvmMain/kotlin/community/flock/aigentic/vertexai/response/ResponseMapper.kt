package community.flock.aigentic.vertexai.response

import com.google.genai.types.Content
import com.google.genai.types.GenerateContentResponse
import com.google.genai.types.GenerateContentResponseUsageMetadata
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.model.Usage
import community.flock.aigentic.vertexai.toJson
import generateRandomString
import kotlin.jvm.optionals.getOrNull

fun GenerateContentResponse.toModelResponse(): ModelResponse {
    val candidate = candidates().getOrNull()?.firstOrNull()
    val promptFeedback = promptFeedback().getOrNull()

    return if (promptFeedback?.blockReason() != null) {
        aigenticException("Gemini blocked the prompt because of reason: '${promptFeedback.blockReason()}'")
    } else if (candidate != null) {
        ModelResponse(
            message = candidate.content().getOrNull()?.toMessages() ?: aigenticException("No message found in response."),
            usage = usageMetadata().getOrNull()?.toUsage() ?: Usage.EMPTY,
        )
    } else {
        aigenticException("No candidate found in Gemini response: $this.")
    }
}

internal fun Content.toMessages(): Message {
    val functionCalls =
        parts().getOrNull()?.mapNotNull {
            it.functionCall().getOrNull()
        } ?: emptyList()

    val toolCalls =
        functionCalls.map {
            val name = it.name().getOrNull() ?: aigenticException("No name found in FunctionCall: $it.")
            val arguments: Map<String, Any> = it.args().getOrNull() ?: aigenticException("No arguments found in FunctionCall: $it.")
            ToolCall(ToolCallId(generateRandomString(20)), name, arguments.toJson())
        }

    return Message.ToolCalls(toolCalls)
}

private fun GenerateContentResponseUsageMetadata.toUsage(): Usage =
    Usage(
        inputTokenCount = promptTokenCount().getOrNull() ?: 0,
        outputTokenCount = candidatesTokenCount().getOrNull() ?: 0,
        thinkingOutputTokenCount = thoughtsTokenCount().getOrNull() ?: 0,
    )
