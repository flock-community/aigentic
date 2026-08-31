package community.flock.aigentic.gemini.mapper

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.gemini.client.model.BlobContent
import community.flock.aigentic.gemini.client.model.Content
import community.flock.aigentic.gemini.client.model.FileDataContent
import community.flock.aigentic.gemini.client.model.FunctionCallContent
import community.flock.aigentic.gemini.client.model.FunctionDeclaration
import community.flock.aigentic.gemini.client.model.FunctionResponseContent
import community.flock.aigentic.gemini.client.model.GenerateContentRequest
import community.flock.aigentic.gemini.client.model.GenerationConfig
import community.flock.aigentic.gemini.client.model.HarmBlockThreshold
import community.flock.aigentic.gemini.client.model.HarmCategory
import community.flock.aigentic.gemini.client.model.Part
import community.flock.aigentic.gemini.client.model.Role
import community.flock.aigentic.gemini.client.model.SafetySettings
import community.flock.aigentic.gemini.client.model.ThinkingConfig
import community.flock.aigentic.gemini.client.model.Tool
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import community.flock.aigentic.gemini.model.resolveThinkingLevel
import community.flock.aigentic.gemini.model.supportsSampling
import community.flock.aigentic.gemini.model.validateGeminiThinkingConfig
import community.flock.aigentic.providers.jsonschema.emitPropertiesAndRequired
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import community.flock.aigentic.core.model.ThinkingConfig as CoreThinkingConfig

internal fun createGenerateContentRequest(
    messages: List<Message>,
    tools: List<ToolDescription>,
    generationSettings: GenerationSettings,
    structuredResponseParameter: Parameter?,
    modelIdentifier: GeminiModelIdentifier,
): GenerateContentRequest =
    GenerateContentRequest(
        systemInstruction = getSystemInstruction(messages),
        generationConfig = generationSettings.toGenerationConfig(structuredResponseParameter, modelIdentifier),
        contents =
            messages.map { message ->
                when (message) {
                    is Message.Url -> {
                        listOf(
                            Part.FileDataPart(FileDataContent(mimeType = message.mimeType.value, fileUri = message.url)),
                        )
                    }

                    is Message.Base64 -> {
                        listOf(
                            Part.Blob(BlobContent(mimeType = message.mimeType.value, data = formatBase64Content(message))),
                        )
                    }

                    is Message.SystemPrompt -> {
                        listOf(
                            Part.Text("See system instruction for your task"),
                        )
                    }

                    // The API returns a 400 when the initial request contains no messages
                    is Message.StructuredOutput -> {
                        listOf(Part.Text(message.response))
                    }

                    is Message.Text -> {
                        listOf<Part>(Part.Text(message.text))
                    }

                    is Message.ExampleToolMessage -> {
                        listOf<Part>(Part.Text(message.text))
                    }

                    is Message.ToolCalls -> {
                        message.toolCalls.map {
                            Part.FunctionCall(
                                FunctionCallContent(
                                    it.name,
                                    Json.decodeFromString(it.arguments),
                                ),
                            )
                        }
                    }

                    is Message.ToolResult -> {
                        listOf(
                            Part.FunctionResponse(
                                FunctionResponseContent(
                                    message.toolName,
                                    buildJsonObject {
                                        put("result", message.response.result)
                                    },
                                ),
                            ),
                        )
                    }
                }.let {
                    Content(message.sender.toRole(), it)
                }
            },
        tools = if (structuredResponseParameter == null) tools.toTools() else null,
        safetySettings = defaultSafetySettings(),
    )

private fun defaultSafetySettings(): List<SafetySettings> =
    HarmCategory.entries.map {
        SafetySettings(
            category = it,
            threshold = HarmBlockThreshold.BLOCK_NONE,
        )
    }

private fun formatBase64Content(message: Message.Base64) = message.base64Content.substringAfter("base64,")

private fun getSystemInstruction(messages: List<Message>): Content =
    messages
        .filterIsInstance<Message.SystemPrompt>()
        .map {
            Part.Text(it.prompt)
        }.let { Content(Role.User, it) }

private fun Sender.toRole(): Role =
    when (this) {
        Sender.Agent -> Role.User
        Sender.Model -> Role.Model
    }

private fun GenerationSettings.toGenerationConfig(
    structuredResponseParameter: Parameter?,
    modelIdentifier: GeminiModelIdentifier,
): GenerationConfig {
    validateGeminiThinkingConfig(modelIdentifier, this)
    val supportsSampling = modelIdentifier.supportsSampling()
    return GenerationConfig(
        temperature = if (supportsSampling) temperature ?: GenerationSettings.DEFAULT_TEMPERATURE else temperature,
        topP = if (supportsSampling) topP ?: GenerationSettings.DEFAULT_TOP_P else topP,
        topK = if (supportsSampling) topK ?: GenerationSettings.DEFAULT_TOP_K else topK,
        candidateCount = if (supportsSampling) 1 else null,
        maxOutputTokens = maxOutputTokens,
        thinkingConfig = thinkingConfig?.takeIf { it.thinkingBudget != null || it.thinkingLevel != null }?.toThinkingConfig(modelIdentifier),
        responseSchema = structuredResponseParameter?.getStructuredResponseSchema(),
        responseMimeType = structuredResponseParameter?.let { "application/json" },
    )
}

private fun CoreThinkingConfig.toThinkingConfig(modelIdentifier: GeminiModelIdentifier): ThinkingConfig =
    ThinkingConfig(
        thinkingBudget = thinkingBudget,
        thinkingLevel = thinkingLevel?.let { modelIdentifier.resolveThinkingLevel(it) }?.name?.lowercase(),
    )

private fun Parameter.getStructuredResponseSchema(): JsonObject? =
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

private fun List<ToolDescription>.toTools(): List<Tool> =
    listOf(
        Tool(
            map {
                FunctionDeclaration(
                    name = it.name.value,
                    description = it.description ?: "",
                    parameters =
                        if (it.parameters.isEmpty()) {
                            null
                        } else {
                            buildJsonObject {
                                put("type", "object")
                                emitPropertiesAndRequired(it.parameters)
                            }
                        },
                )
            },
        ),
    )
