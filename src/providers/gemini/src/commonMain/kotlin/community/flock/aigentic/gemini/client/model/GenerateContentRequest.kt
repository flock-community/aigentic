package community.flock.aigentic.gemini.client.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val tools: List<Tool>? = null,
    @SerialName("tool_config") var toolConfig: ToolConfig? = null,
    @SerialName("system_instruction") val systemInstruction: Content? = null,
)

@Serializable
data class GenerationConfig(
    val temperature: Float?,
    @SerialName("top_p") val topP: Float?,
    @SerialName("top_k") val topK: Int?,
    @SerialName("candidate_count") val candidateCount: Int?,
    @SerialName("max_output_tokens") val maxOutputTokens: Int?,
    @SerialName("stop_sequences") val stopSequences: List<String>?,
    @SerialName("response_mime_type") val responseMimeType: String? = null,
    @SerialName("presence_penalty") val presencePenalty: Float? = null,
    @SerialName("frequency_penalty") val frequencyPenalty: Float? = null,
)

@Serializable
data class ToolConfig(
    @SerialName("function_calling_config") val functionCallingConfig: FunctionCallingConfig,
)

@Serializable
data class FunctionCallingConfig(val mode: Mode) {
    @Serializable
    enum class Mode {
        @SerialName("MODE_UNSPECIFIED")
        UNSPECIFIED,
        AUTO,
        ANY,
        NONE,
    }
}

@Serializable
class Tool(
    val functionDeclarations: List<FunctionDeclaration>,
)

@Serializable
data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: JsonObject?,
)

@Serializable
data class Schema(
    val type: String,
    val description: String? = null,
    val format: String? = null,
    val enum: List<String>? = null,
    val properties: Map<String, Schema>? = null,
    val required: List<String>? = null,
    val items: Schema? = null,
)

@Serializable
@JvmInline
value class Role(val value: String) {
    public companion object {
        public val User = Role("user")
        public val Model = Role("model")
    }
}
