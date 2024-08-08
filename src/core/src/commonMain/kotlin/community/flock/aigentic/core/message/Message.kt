package community.flock.aigentic.core.message

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline

sealed class Message(
    open val sender: Sender,
) {
    data class SystemPrompt(
        val prompt: String,
    ) : Message(Sender.Agent)

    data class Text(
        override val sender: Sender,
        val text: String,
    ) : Message(sender)

    data class ImageUrl(
        override val sender: Sender,
        val url: String,
        val mimeType: MimeType,
    ) : Message(Sender.Agent)

    data class ImageBase64(
        override val sender: Sender,
        val base64Content: String,
        val mimeType: MimeType,
    ) : Message(Sender.Agent)

    data class ToolCalls(
        val toolCalls: List<ToolCall>,
    ) : Message(Sender.Model)

    data class ToolResult(
        val toolCallId: ToolCallId,
        val toolName: String,
        val response: ToolResultContent,
    ) : Message(Sender.Agent)
}

data class ToolCall(
    val id: ToolCallId,
    val name: String,
    val arguments: String,
)

@JvmInline
value class MimeType private constructor(val value: String) {
    companion object {
        val JPEG = MimeType("image/jpeg")
        val PNG = MimeType("image/png")
        val WEBP = MimeType("image/webp")
        val HEIC = MimeType("image/heic")
        val HEIF = MimeType("image/heif")
    }
}

@JvmInline
value class ToolCallId(val id: String)

@JvmInline
value class ToolResultContent(val result: String)

sealed interface Sender {
    data object Agent : Sender
    data object Model : Sender
}

fun ToolCall.argumentsAsJson(json: Json = Json): JsonObject = json.decodeFromString(arguments)
