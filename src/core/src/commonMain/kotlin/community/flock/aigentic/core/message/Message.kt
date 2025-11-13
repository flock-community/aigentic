package community.flock.aigentic.core.message

import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant

sealed interface ContextMessage

/**
 * Base class for all messages in the agent system.
 *
 * @property sender Who created this message (Agent or Model)
 * @property category What purpose this message serves
 * @property createdAt When this message was created
 */
sealed class Message(
    open val sender: Sender,
    open val category: MessageCategory,
    open val createdAt: Instant = Clock.System.now(),
) {
    /**
     * Creates a copy of this message with a different category.
     * Useful for recategorizing messages.
     */
    abstract fun withCategory(newCategory: MessageCategory): Message

    data class SystemPrompt(
        val prompt: String,
        override val createdAt: Instant = Clock.System.now(),
    ) : Message(Sender.Agent, MessageCategory.SYSTEM_PROMPT, createdAt) {
        override fun withCategory(newCategory: MessageCategory) = copy() // System prompts can't change category
    }

    data class Text(
        override val sender: Sender,
        override val category: MessageCategory = MessageCategory.EXECUTION,
        val text: String,
        override val createdAt: Instant = Clock.System.now(),
    ) : Message(sender, category, createdAt), ContextMessage {
        override fun withCategory(newCategory: MessageCategory) = copy(category = newCategory)
    }

    data class Url(
        override val sender: Sender,
        override val category: MessageCategory = MessageCategory.EXECUTION,
        val url: String,
        val mimeType: MimeType,
        override val createdAt: Instant = Clock.System.now(),
    ) : Message(sender, category, createdAt), ContextMessage {
        override fun withCategory(newCategory: MessageCategory) = copy(category = newCategory)
    }

    data class Base64(
        override val sender: Sender,
        override val category: MessageCategory = MessageCategory.EXECUTION,
        val base64Content: String,
        val mimeType: MimeType,
        override val createdAt: Instant = Clock.System.now(),
    ) : Message(sender, category, createdAt), ContextMessage {
        override fun withCategory(newCategory: MessageCategory) = copy(category = newCategory)
    }

    data class ToolCalls(
        val toolCalls: List<ToolCall>,
        override val createdAt: Instant = Clock.System.now(),
    ) : Message(Sender.Model, MessageCategory.EXECUTION, createdAt) {
        override fun withCategory(newCategory: MessageCategory) = copy() // Tool calls are always EXECUTION
    }

    data class StructuredOutput(
        val response: String,
        override val createdAt: Instant = Clock.System.now(),
    ) : Message(Sender.Model, MessageCategory.EXECUTION, createdAt) {
        override fun withCategory(newCategory: MessageCategory) = copy() // Structured outputs are always EXECUTION
    }

    data class ToolResult(
        val toolCallId: ToolCallId,
        val toolName: String,
        val response: ToolResultContent,
        override val createdAt: Instant = Clock.System.now(),
    ) : Message(Sender.Agent, MessageCategory.EXECUTION, createdAt) {
        override fun withCategory(newCategory: MessageCategory) = copy() // Tool results are always EXECUTION
    }

    data class ExampleToolMessage(
        override val sender: Sender,
        val text: String,
        val id: ToolCallId? = null,
        override val createdAt: Instant = Clock.System.now(),
    ) : Message(sender, MessageCategory.EXAMPLE, createdAt), ContextMessage {
        override fun withCategory(newCategory: MessageCategory) = copy() // Example messages are always EXAMPLE
    }
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
