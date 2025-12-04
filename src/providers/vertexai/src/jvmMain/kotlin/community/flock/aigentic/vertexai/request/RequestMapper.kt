package community.flock.aigentic.vertexai.request

import com.fasterxml.jackson.databind.JsonNode
import com.google.genai.JsonSerializable
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
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.providers.jsonschema.emitPropertiesAndRequired
import community.flock.aigentic.vertexai.fromJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Base64

internal fun createRequestContents(messages: List<Message>): List<Content> =
    messages.map { message ->

        val parts: List<Part> =
            when (message) {
                is Message.Base64 -> listOf(Part.fromBytes(Base64.getDecoder().decode(message.base64Content), message.mimeType.value))
                is Message.ExampleToolMessage -> listOf(Part.fromText(message.text))
                is Message.SystemPrompt -> listOf(Part.fromText("See system instruction for your task"))
                is Message.Text -> listOf(Part.fromText(message.text))
                is Message.StructuredOutput -> listOf(Part.fromText(message.response))
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
    structuredOutputParameter: Parameter?,
): GenerateContentConfig =
    GenerateContentConfig.builder()
        .systemInstruction(getSystemInstruction(messages))
        .temperature(generationSettings.temperature)
        .topP(generationSettings.topP)
        .topK(generationSettings.topK.toFloat())
        .candidateCount(1)
        .tools(if (structuredOutputParameter == null) tools.toVertexTools() else emptyList())
        .safetySettings(createSafetySettings())
        .withThinkingConfig(generationSettings.thinkingConfig)
        .apply {
            structuredOutputParameter?.let { param ->
                responseMimeType("application/json")
                responseJsonSchema(param.getStructuredResponseSchema().toJsonNode())
            }
        }
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

private fun Sender.toVertexRole(): String =
    when (this) {
        Sender.Agent -> "user"
        Sender.Model -> "model"
    }

private fun Parameter.getStructuredResponseSchema(): JsonObject =
    this.let { responseParam ->
        buildJsonObject {
            put("type", "object")
            emitPropertiesAndRequired(
                when (responseParam) {
                    is Parameter.Complex.Object -> responseParam.parameters
                    else -> listOf(responseParam)
                },
            )
        }
    }

inline fun <reified T> T.toJsonNode(): JsonNode = JsonSerializable.stringToJsonNode(Json.encodeToString(this))
