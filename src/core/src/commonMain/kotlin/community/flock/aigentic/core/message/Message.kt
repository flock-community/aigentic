package community.flock.aigentic.core.message

import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant

sealed interface ContextMessage

sealed class Message(
    open val sender: Sender,
    open val category: MessageCategory,
    open val createdAt: Instant = Clock.System.now(),
) {
    data class SystemPrompt(
        val prompt: String,
        override val category: MessageCategory = MessageCategory.SYSTEM_PROMPT,
    ) : Message(Sender.Agent, category)

    data class Text(
        override val sender: Sender,
        val text: String,
        override val category: MessageCategory,
    ) : Message(sender, category),
        ContextMessage

    data class Url(
        override val sender: Sender,
        val url: String,
        val mimeType: MimeType,
        override val category: MessageCategory,
    ) : Message(Sender.Agent, category),
        ContextMessage

    data class Base64(
        override val sender: Sender,
        val base64Content: String,
        val mimeType: MimeType,
        override val category: MessageCategory,
    ) : Message(Sender.Agent, category),
        ContextMessage

    data class ToolCalls(
        val toolCalls: List<ToolCall>,
        override val category: MessageCategory = MessageCategory.EXECUTION,
    ) : Message(Sender.Model, category)

    data class StructuredOutput(
        val response: String,
        override val category: MessageCategory = MessageCategory.EXECUTION,
    ) : Message(Sender.Model, category)

    data class ToolResult(
        val toolCallId: ToolCallId,
        val toolName: String,
        val response: ToolResultContent,
        override val category: MessageCategory = MessageCategory.EXECUTION,
    ) : Message(Sender.Agent, category)

    data class ExampleToolMessage(
        override val sender: Sender,
        val text: String,
        val id: ToolCallId? = null,
        override val category: MessageCategory = MessageCategory.EXAMPLE,
    ) : Message(sender, category),
        ContextMessage
}

data class ToolCall(
    val id: ToolCallId,
    val name: String,
    val arguments: String,
    val thoughtSignature: String? = null,
)

enum class MimeType(
    val value: String,
) {
    JPEG("image/jpeg"),
    PNG("image/png"),
    WEBP("image/webp"),
    HEIC("image/heic"),
    HEIF("image/heif"),
    PDF("application/pdf"),
}

@JvmInline
value class ToolCallId(
    val id: String,
)

/**
 * What a tool returned to the model: one or more [ToolResultPart]s, text and/or images. A provider that
 * cannot show images to the model sends [result], which writes each image as a short placeholder.
 */
data class ToolResultContent(
    val parts: List<ToolResultPart>,
) {
    constructor(result: String) : this(listOf(ToolResultPart.Text(result)))

    val result: String
        get() =
            parts.joinToString("\n") {
                when (it) {
                    is ToolResultPart.Text -> it.text
                    is ToolResultPart.Image -> "[image: ${it.mimeType.value}]"
                }
            }

    val hasImages: Boolean get() = parts.any { it is ToolResultPart.Image }
}

sealed interface ToolResultPart {
    data class Text(
        val text: String,
    ) : ToolResultPart

    /** [base64Content] is the bare base64 of the file, without a `data:` prefix. */
    data class Image(
        val base64Content: String,
        val mimeType: MimeType,
    ) : ToolResultPart
}

sealed interface Sender {
    data object Agent : Sender

    data object Model : Sender
}

@OptIn(ExperimentalSerializationApi::class)
private val tolerantJson = Json { allowTrailingComma = true }

fun ToolCall.argumentsAsJson(json: Json = tolerantJson): JsonObject = json.decodeFromString(arguments)

fun Message.StructuredOutput.asJson(json: Json = tolerantJson): JsonObject = json.decodeFromString(response)

fun List<Message>.mapToTextMessages(): List<Message.ExampleToolMessage> {
    val messageArguments: List<Message.ExampleToolMessage> =
        this.filterIsInstance<Message.ToolCalls>().filterNot { it.toolCalls.first().name == FINISHED_TASK_TOOL_NAME }.flatMap {
            it.toolCalls.map { tool ->
                Message.ExampleToolMessage(id = tool.id, text = "Tool call with arguments: " + tool.arguments, sender = Sender.Agent)
            }
        }

    val messageResults: List<Message.ExampleToolMessage> =
        this
            .filterIsInstance<Message.ToolResult>()
            .map { Message.ExampleToolMessage(id = it.toolCallId, text = "Tool call result: " + it.response.result, sender = Sender.Agent) }

    val joinedMessages = messageArguments.zip(messageResults).flatMap { listOf(it.first, it.second) }
    return joinedMessages
}
