package community.flock.aigentic.koog

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Data classes mirroring the Aigentic Platform contract defined in
 * `src/platform/wirespec/gateway.ws` (the `POST RunDto -> /gateway/runs` endpoint).
 *
 * In a production integration these would be the Wirespec-generated types from the
 * published `community.flock.aigentic:platform` artifact. They are reproduced here so the
 * proof-of-concept stays self-contained and can build against Koog (Kotlin 2.3.x) without
 * pulling the Kotlin-2.1.x Aigentic build into the same Gradle build.
 */
val aigenticJson: Json =
    Json {
        classDiscriminator = "type"
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
        prettyPrint = true
    }

@Serializable
data class RunDto(
    val startedAt: String,
    val finishedAt: String,
    val config: ConfigDto,
    val result: ResultDto,
    val messages: List<MessageDto>,
    val modelRequests: List<ModelRequestInfoDto>,
)

@Serializable
data class ConfigDto(
    val task: TaskDto,
    val modelIdentifier: String,
    val systemPrompt: String,
    val tools: List<ToolDto>,
    val exampleRunIds: List<String>? = null,
    val responseJsonSchema: String? = null,
    val temperature: Double,
    val thinkingBudget: Long? = null,
)

@Serializable
data class TaskDto(
    val description: String,
    val instructions: List<String>,
)

@Serializable
data class ToolDto(
    val name: String,
    val description: String? = null,
    val parameters: List<ParameterDto>,
)

@Serializable
data class ParameterDto(
    val name: String,
    val description: String? = null,
    val isRequired: Boolean,
    val paramType: String,
)

@Serializable
data class ModelRequestInfoDto(
    val startedAt: String,
    val finishedAt: String,
    val inputTokenCount: Int,
    val outputTokenCount: Int,
    val thinkingOutputTokenCount: Int? = null,
    val cachedInputTokenCount: Int? = null,
)

enum class SenderDto { Agent, Model }

enum class MessageCategoryDto { SYSTEM_PROMPT, CONFIG_CONTEXT, RUN_CONTEXT, EXAMPLE, EXECUTION }

@Serializable
sealed interface MessageDto {
    val createdAt: String
    val sender: SenderDto
    val category: MessageCategoryDto?
}

@Serializable
@SerialName("SystemPromptMessageDto")
data class SystemPromptMessageDto(
    override val createdAt: String,
    override val sender: SenderDto,
    val prompt: String,
    override val category: MessageCategoryDto? = null,
) : MessageDto

@Serializable
@SerialName("TextMessageDto")
data class TextMessageDto(
    override val createdAt: String,
    override val sender: SenderDto,
    val text: String,
    override val category: MessageCategoryDto? = null,
) : MessageDto

@Serializable
@SerialName("ToolCallsMessageDto")
data class ToolCallsMessageDto(
    override val createdAt: String,
    override val sender: SenderDto,
    val toolCalls: List<ToolCallDto>,
    override val category: MessageCategoryDto? = null,
) : MessageDto

@Serializable
@SerialName("ToolResultMessageDto")
data class ToolResultMessageDto(
    override val createdAt: String,
    override val sender: SenderDto,
    val toolCallId: String,
    val toolName: String,
    val response: String,
    override val category: MessageCategoryDto? = null,
) : MessageDto

@Serializable
data class ToolCallDto(
    val id: String,
    val name: String,
    val arguments: String,
    val expectedArguments: String? = null,
)

@Serializable
sealed interface ResultDto

@Serializable
@SerialName("FinishedResultDto")
data class FinishedResultDto(
    val description: String,
    val response: String? = null,
) : ResultDto

@Serializable
@SerialName("StuckResultDto")
data class StuckResultDto(
    val reason: String,
) : ResultDto

@Serializable
@SerialName("FatalResultDto")
data class FatalResultDto(
    val message: String,
) : ResultDto
