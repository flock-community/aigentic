package community.flock.aigentic.core.message

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline

sealed class Message(
    open val sender: Sender,
) {
    data class SystemPrompt(
        val prompt: String,
    ) : Message(Sender.Aigentic)

    data class Text(
        override val sender: Sender,
        val text: String,
    ) : Message(sender)

    data class Image(
        override val sender: Sender,
        val image: String,
    ) : Message(Sender.Aigentic)

    data class ToolCalls(
        val toolCalls: List<ToolCall>,
    ) : Message(Sender.Model)

    data class ToolResult(
        val toolCallId: ToolCallId,
        val toolName: String,
        val response: ToolResultContent,
    ) : Message(Sender.Aigentic)
}

data class ToolCall(
    val id: ToolCallId,
    val name: String,
    val arguments: String,
)

@JvmInline
value class ToolCallId(val id: String)

@JvmInline
value class ToolResultContent(val result: String)

sealed interface Sender {
    data object Aigentic : Sender
    data object Model : Sender
}

fun ToolCall.argumentsAsJson(json: Json = Json): JsonObject = json.decodeFromString(arguments)
