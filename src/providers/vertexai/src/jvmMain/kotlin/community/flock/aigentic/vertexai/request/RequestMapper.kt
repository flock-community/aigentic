package community.flock.aigentic.vertexai.request

import com.google.genai.types.Content
import com.google.genai.types.FunctionDeclaration
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.HarmBlockThreshold
import com.google.genai.types.HarmCategory
import com.google.genai.types.Part
import com.google.genai.types.SafetySetting
import com.google.genai.types.Schema
import com.google.genai.types.Tool
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.ThinkingConfig
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.providers.jsonschema.emitPropertiesAndRequired
import community.flock.aigentic.vertexai.fromJson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal fun createRequestContents(messages: List<Message>): List<Content> =
    messages.map { message ->

        val parts: List<Part> =
            when (message) {
                is Message.Base64 -> listOf(Part.fromUri(formatBase64Content(message), message.mimeType.value))
                is Message.ExampleToolMessage -> listOf(Part.fromText(message.text))
                is Message.SystemPrompt -> listOf(Part.fromText("See system instruction for your task"))
                is Message.Text -> listOf(Part.fromText(message.text))
                is Message.ToolCalls ->
                    message.toolCalls.map {
                        Part.fromFunctionCall(it.name, it.arguments.fromJson())
                    }

                is Message.ToolResult ->
                    listOf(
                        Part.fromFunctionResponse(
                            message.toolName,
                            buildJsonObject {
                                put("result", message.response.result)
                            },
                        ),
                    )

                is Message.Url -> listOf(Part.fromUri(message.url, message.mimeType.value))
            }

        Content.builder().role(message.sender.toVertexRole()).parts(parts).build()
    }

internal fun createGenerateConfig(
    messages: List<Message>,
    tools: List<ToolDescription>,
    generationSettings: GenerationSettings,
): GenerateContentConfig =
    GenerateContentConfig.builder()
        .systemInstruction(getSystemInstruction(messages))
        .temperature(generationSettings.temperature)
        .topP(generationSettings.topP)
        .topK(generationSettings.topK.toFloat())
        .candidateCount(1)
        .tools(tools.toVertexTools())
        .safetySettings(createSafetySettings())
        .withThinkingConfig(generationSettings.thinkingConfig)
        .build()

private fun List<ToolDescription>.toVertexTools(): List<Tool> {
    val declarations =
        map { toolDescription ->

            val functionDeclaration =
                FunctionDeclaration.builder()
                    .name(toolDescription.name.value)
                    .description(toolDescription.description ?: "")
                    .parameters(Schema.fromJson(getToolParametersJson(toolDescription)))
                    .build()

            functionDeclaration
        }

    return listOf(
        Tool.builder()
            .functionDeclarations(declarations).build(),
    )
}

private fun GenerateContentConfig.Builder.withThinkingConfig(thinkingConfig: ThinkingConfig?): GenerateContentConfig.Builder =
    apply {
        thinkingConfig?.let {
            thinkingConfig(
                com.google.genai.types.ThinkingConfig.builder()
                    .thinkingBudget(it.thinkingBudget)
                    .build(),
            )
        }
    }

private fun getToolParametersJson(toolDescription: ToolDescription): String =
    if (toolDescription.parameters.isEmpty()) {
        null
    } else {
        buildJsonObject {
            put("type", "object")
            emitPropertiesAndRequired(toolDescription.parameters)
        }
    }.let { Json.encodeToString(it) }

private fun getSystemInstruction(messages: List<Message>): Content =
    messages.filterIsInstance<Message.SystemPrompt>().map {
        Part.builder().text(it.prompt).build()
    }.let { Content.builder().role(Sender.Agent.toVertexRole()).parts(it).build() }

private fun createSafetySettings(): List<SafetySetting> =
    HarmCategory.Known.entries
        .map { category ->
            SafetySetting.builder()
                .category(category)
                .threshold(HarmBlockThreshold.Known.BLOCK_NONE)
                .build()
        }

private fun formatBase64Content(message: Message.Base64) = message.base64Content.substringAfter("base64,")

private fun Sender.toVertexRole(): String =
    when (this) {
        Sender.Agent -> "user"
        Sender.Model -> "model"
    }
