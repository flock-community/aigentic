package community.flock.aigentic.mapper

import com.aallam.openai.api.chat.ChatCompletion
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.mapper.DomainMapper.toMessage

internal fun ChatCompletion.toModelResponse(): ModelResponse {
    return ModelResponse(
        message = choices.first().message.toMessage(),
    )
}
