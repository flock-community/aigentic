package community.flock.aigentic.openai.mapper

import com.aallam.openai.api.chat.ChatCompletion
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.openai.mapper.DomainMapper.toMessage

internal fun ChatCompletion.toModelResponse(): ModelResponse {
    return ModelResponse(
        message = choices.first().message.toMessage(),
    )
}
