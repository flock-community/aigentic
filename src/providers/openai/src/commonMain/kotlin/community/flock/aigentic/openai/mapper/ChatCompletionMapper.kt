package community.flock.aigentic.openai.mapper

import com.aallam.openai.api.chat.ChatCompletion
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.model.Usage
import community.flock.aigentic.openai.mapper.DomainMapper.toMessage
import com.aallam.openai.api.core.Usage as OpenAIUsage

internal fun ChatCompletion.toModelResponse(): ModelResponse {
    return ModelResponse(
        message = choices.first().message.toMessage(),
        usage = usage?.toUsage() ?: Usage.EMPTY,
    )
}

private fun OpenAIUsage.toUsage(): Usage {
    return Usage(
        inputTokenCount = promptTokens ?: 0,
        outputTokenCount = completionTokens ?: 0,
    )
}
