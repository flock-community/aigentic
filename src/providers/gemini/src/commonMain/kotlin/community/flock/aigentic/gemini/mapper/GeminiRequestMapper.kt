package community.flock.aigentic.gemini.mapper

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.gemini.client.model.BlobContent
import community.flock.aigentic.gemini.client.model.Content
import community.flock.aigentic.gemini.client.model.FileDataContent
import community.flock.aigentic.gemini.client.model.FunctionCallContent
import community.flock.aigentic.gemini.client.model.FunctionDeclaration
import community.flock.aigentic.gemini.client.model.FunctionResponseContent
import community.flock.aigentic.gemini.client.model.GenerateContentRequest
import community.flock.aigentic.gemini.client.model.Part
import community.flock.aigentic.gemini.client.model.Role
import community.flock.aigentic.gemini.client.model.Tool
import community.flock.aigentic.providers.jsonschema.emitPropertiesAndRequired
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal fun createGenerateContentRequest(
    messages: List<Message>,
    tools: List<ToolDescription>,
): GenerateContentRequest =
    GenerateContentRequest(
        systemInstruction = getSystemInstruction(messages),
        contents =
            messages.map { message ->
                when (message) {
                    is Message.ImageUrl ->
                        listOf(
                            Part.FileDataPart(FileDataContent(mimeType = message.mimeType.value, fileUri = message.url)),
                        )
                    is Message.ImageBase64 ->
                        listOf(
                            Part.Blob(BlobContent(mimeType = message.mimeType.value, data = formatBase64Content(message))),
                        )
                    is Message.SystemPrompt ->
                        listOf(
                            Part.Text("See system instruction"),
                        ) // The API returns a 400 when the initial request contains no messages
                    is Message.Text -> listOf<Part>(Part.Text(message.text))
                    is Message.ToolCalls ->
                        message.toolCalls.map {
                            Part.FunctionCall(
                                FunctionCallContent(
                                    it.name,
                                    Json.decodeFromString(it.arguments),
                                ),
                            )
                        }
                    is Message.ToolResult ->
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
                }.let {
                    Content(message.sender.toRole(), it)
                }
            },
        tools =
            listOf(
                Tool(
                    tools.map {
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
            ),
    )

private fun formatBase64Content(message: Message.ImageBase64) = message.base64Content.substringAfter("base64,")

private fun getSystemInstruction(messages: List<Message>): Content =
    messages.filterIsInstance<Message.SystemPrompt>().map {
        Part.Text(it.prompt)
    }.let { Content(Role.User, it) }

private fun Sender.toRole(): Role =
    when (this) {
        Sender.Agent -> Role.User
        Sender.Model -> Role.Model
    }
