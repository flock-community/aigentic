package community.flock.aigentic.request

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ToolChoice
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.model.ModelId
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.mapper.OpenAIMapper.toOpenAIMessage
import community.flock.aigentic.mapper.toOpenAITool
import community.flock.aigentic.model.OpenAIModelIdentifier

internal fun createChatCompletionsRequest(
    messages: List<Message>,
    tools: List<ToolDescription>,
    openAIModelIdentifier: OpenAIModelIdentifier,
): ChatCompletionRequest {
    return chatCompletionRequest {
//        temperature = 0.0
        model = ModelId(openAIModelIdentifier.stringValue)
        this.messages = messages.map { it.toOpenAIMessage() }
        this.tools = tools.map { it.toOpenAITool() }
        toolChoice = ToolChoice.Auto
    }
}
