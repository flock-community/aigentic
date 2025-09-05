package community.flock.aigentic.core.message

import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant

sealed interface ContextMessage

sealed interface MessageType {
    data object New : MessageType
    data object Example : MessageType
}

sealed class Message(
    open val sender: Sender,
    open val messageType: MessageType,
    open val createdAt: Instant = Clock.System.now(),
) {
    data class SystemPrompt(
        val prompt: String,
    ) : Message(Sender.Agent, MessageType.New)

    data class Text(
        override val sender: Sender,
        override val messageType: MessageType,
        val text: String,
    ) : Message(sender, messageType), ContextMessage

    data class Url(
        override val sender: Sender,
        override val messageType: MessageType,
        val url: String,
        val mimeType: MimeType,
    ) : Message(Sender.Agent, messageType), ContextMessage

    data class Base64(
        override val sender: Sender,
        override val messageType: MessageType,
        val base64Content: String,
        val mimeType: MimeType,
    ) : Message(Sender.Agent, messageType), ContextMessage

    data class ToolCalls(
        val toolCalls: List<ToolCall>,
    ) : Message(Sender.Model, MessageType.New)

    data class StructuredOutput(
        val response: String,
    ) : Message(Sender.Model, MessageType.New)

    data class ToolResult(
        val toolCallId: ToolCallId,
        val toolName: String,
        val response: ToolResultContent,
    ) : Message(Sender.Agent, MessageType.New)

    data class ExampleToolMessage(
        override val sender: Sender,
        val text: String,
        val id: ToolCallId? = null,
    ) : Message(sender, MessageType.Example), ContextMessage
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

fun Message.StructuredOutput.asJson(json: Json = Json): JsonObject = json.decodeFromString(response)

fun List<Message>.mapToTextMessages(): List<Message.ExampleToolMessage> {
    val messageArguments: List<Message.ExampleToolMessage> =
        this.filterIsInstance<Message.ToolCalls>().filterNot { it.toolCalls.first().name == FINISHED_TASK_TOOL_NAME }.flatMap {
            it.toolCalls.map { tool ->
                Message.ExampleToolMessage(id = tool.id, text = "Tool call with arguments: " + tool.arguments, sender = Sender.Agent)
            }
        }

    val messageResults: List<Message.ExampleToolMessage> =
        this.filterIsInstance<Message.ToolResult>()
            .map { Message.ExampleToolMessage(id = it.toolCallId, text = "Tool call result: " + it.response.result, sender = Sender.Agent) }

    val joinedMessages = messageArguments.zip(messageResults).flatMap { listOf(it.first, it.second) }
    return joinedMessages
}
