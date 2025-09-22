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

fun GenerateContentResponse.toModelResponse(isStructuredOutput: Boolean): ModelResponse {
    promptFeedback().getOrNull()?.blockReason()?.let {
        aigenticException("VertexAI blocked the prompt because of reason: '$it'")
    }

    val candidate =
        candidates().getOrNull()?.firstOrNull()
            ?: aigenticException("No candidate found in VertexAI response: $this.")

    val content =
        candidate.content().getOrNull()
            ?: aigenticException("No message found in response.")

    val usage = usageMetadata().getOrNull()?.toUsage() ?: Usage.EMPTY
    val message = content.toMessage(isStructuredOutput)

    return ModelResponse(message = message, usage = usage)
}

private fun Content.toMessage(isStructuredOutput: Boolean): Message =
    when {
        isStructuredOutput -> toStructuredOutputMessage()
        else -> toToolCallsMessage()
    }

private fun Content.toStructuredOutputMessage(): Message {
    val textPart = parts().getOrNull()?.firstOrNull { it.text().getOrNull() != null }
    val text =
        textPart?.text()?.getOrNull()
            ?: aigenticException("Expected a text part in VertexAI structured output response, but found none.")
    return Message.StructuredOutput(text)
}

private fun Content.toToolCallsMessage(): Message {
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
