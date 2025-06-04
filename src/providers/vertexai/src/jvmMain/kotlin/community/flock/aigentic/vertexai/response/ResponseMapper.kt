package community.flock.aigentic.vertexai.response

import com.google.genai.types.Content
import com.google.genai.types.FunctionCall
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
    promptFeedback().getOrNull()?.blockReason()?.let {
        aigenticException("VertexAI blocked the prompt because of reason: '$it'")
    }

    val candidate =
        candidates().getOrNull()?.firstOrNull()
            ?: aigenticException("No candidate found in VertexAI response: $this.")

    return ModelResponse(
        message = candidate.content().getOrNull()?.toMessages() ?: aigenticException("No message found in response."),
        usage = usageMetadata().getOrNull()?.toUsage() ?: Usage.EMPTY,
    )
}

internal fun Content.toMessages(): Message {
    val toolCalls =
        parts().getOrNull()
            ?.mapNotNull { it.functionCall().getOrNull() }
            ?.map(::toToolCall)
            ?: emptyList()

    return Message.ToolCalls(toolCalls)
}

private fun toToolCall(call: FunctionCall): ToolCall {
    val name =
        call.name().getOrNull()
            ?: aigenticException("No name found in VertexAI FunctionCall: $call.")
    val args =
        call.args().getOrNull()
            ?: aigenticException("No arguments found in VertexAI FunctionCall: $call.")

    return ToolCall(
        id = ToolCallId(generateRandomString(20)),
        name = name,
        arguments = args.toJson(),
    )
}

private fun GenerateContentResponseUsageMetadata.toUsage(): Usage =
    Usage(
        inputTokenCount = promptTokenCount().getOrNull() ?: 0,
        outputTokenCount = candidatesTokenCount().getOrNull() ?: 0,
        thinkingOutputTokenCount = thoughtsTokenCount().getOrNull() ?: 0,
        cachedInputTokenCount = cachedContentTokenCount().getOrNull() ?: 0,
    )
