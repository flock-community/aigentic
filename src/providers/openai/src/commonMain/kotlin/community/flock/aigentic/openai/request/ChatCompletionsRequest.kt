package community.flock.aigentic.openai.request

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.Effort
import com.aallam.openai.api.chat.ToolChoice
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.model.ModelId
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.openai.mapper.OpenAIMapper.toOpenAIMessage
import community.flock.aigentic.openai.mapper.toOpenAITool
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
import community.flock.aigentic.openai.model.resolveThinkingLevel
import community.flock.aigentic.openai.model.usesMaxCompletionTokens
import community.flock.aigentic.openai.model.validateOpenAIThinkingConfig

internal fun createChatCompletionsRequest(
    messages: List<Message>,
    tools: List<ToolDescription>,
    openAIModelIdentifier: ModelIdentifier,
    generationSettings: GenerationSettings,
): ChatCompletionRequest {
    validateOpenAIThinkingConfig(openAIModelIdentifier, generationSettings)
    return chatCompletionRequest {
        temperature = generationSettings.temperature.toDouble()
        topP = generationSettings.topP.toDouble()
        generationSettings.maxOutputTokens?.let {
            if (openAIModelIdentifier.usesMaxCompletionTokens()) maxCompletionTokens = it else maxTokens = it
        }
        // topK = generationSettings.topK, TODO: Top k currently not supported in OpenAI
        model = ModelId(openAIModelIdentifier.stringValue)
        this.messages = messages.map { it.toOpenAIMessage() }
        this.tools = tools.map { it.toOpenAITool() }
        toolChoice = ToolChoice.Auto
        generationSettings.thinkingConfig?.thinkingLevel?.let { level ->
            val resolvedLevel = (openAIModelIdentifier as? OpenAIModelIdentifier)?.resolveThinkingLevel(level) ?: level
            reasoningEffort = Effort(resolvedLevel.name.lowercase())
        }
    }
}
