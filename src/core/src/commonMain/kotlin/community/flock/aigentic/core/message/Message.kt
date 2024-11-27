package community.flock.aigentic.core.message

import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline

sealed interface ContextMessage

sealed class Message(
    open val sender: Sender,
    open val createdAt: Instant = Clock.System.now(),
) {
    data class SystemPrompt(
        val prompt: String,
    ) : Message(Sender.Agent)

    data class Text(
        override val sender: Sender,
        val text: String,
    ) : Message(sender), ContextMessage

    data class Url(
        override val sender: Sender,
        val url: String,
        val mimeType: MimeType,
    ) : Message(Sender.Agent), ContextMessage

    data class Base64(
        override val sender: Sender,
        val base64Content: String,
        val mimeType: MimeType,
    ) : Message(Sender.Agent), ContextMessage

    data class ToolCalls(
        val toolCalls: List<ToolCall>,
    ) : Message(Sender.Model)

    data class ToolResult(
        val toolCallId: ToolCallId,
        val toolName: String,
        val response: ToolResultContent,
    ) : Message(Sender.Agent)

    data class ExampleMessage(
        override val sender: Sender,
        val text: String,
        val id: ToolCallId? = null,
    ) : Message(sender), ContextMessage
}

data class ToolCall(
    val id: ToolCallId,
    val name: String,
    val arguments: String,
)

enum class MimeType(val value: String) {
    JPEG("image/jpeg"),
    PNG("image/png"),
    WEBP("image/webp"),
    HEIC("image/heic"),
    HEIF("image/heif"),
    PDF("application/pdf"),
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

fun List<Message>.mapToTextMessages(): List<Message.ExampleMessage> {
    val messageArguments: List<Message.ExampleMessage> =
        this.filterIsInstance<Message.ToolCalls>().filterNot { it.toolCalls.first().name == FINISHED_TASK_TOOL_NAME }.flatMap {
                it.toolCalls.map { tool ->
                    Message.ExampleMessage(id = tool.id, text = tool.arguments, sender = Sender.Agent)
                }
            }

    val messageResults: List<Message.ExampleMessage> = this.filterIsInstance<Message.ToolResult>()
        .map { Message.ExampleMessage(id = it.toolCallId, text = it.response.result, sender = Sender.Agent) }

    val joinedMessages = messageArguments.zip(messageResults).flatMap { listOf(it.first, it.second) }
    return joinedMessages
}
