package community.flock.aigentic.gemini.client.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val tools: List<Tool>? = null,
    val generationConfig: GenerationConfig,
    @SerialName("tool_config") var toolConfig: ToolConfig? = null,
    @SerialName("system_instruction") val systemInstruction: Content? = null,
    val safetySettings: List<SafetySettings> = emptyList(),
)

@Serializable
data class SafetySettings(
    val category: HarmCategory,
    val threshold: HarmBlockThreshold,
)

enum class HarmCategory {
    HARM_CATEGORY_HARASSMENT,
    HARM_CATEGORY_HATE_SPEECH,
    HARM_CATEGORY_SEXUALLY_EXPLICIT,
    HARM_CATEGORY_DANGEROUS_CONTENT,
    // TODO the following are available in the API docs but trigger a 400 error
//    HARM_CATEGORY_UNSPECIFIED,
//    HARM_CATEGORY_DEROGATORY,
//    HARM_CATEGORY_TOXICITY,
//    HARM_CATEGORY_VIOLENCE,
//    HARM_CATEGORY_SEXUAL,
//    HARM_CATEGORY_MEDICAL,
//    HARM_CATEGORY_DANGEROUS,
}

enum class HarmBlockThreshold {
    HARM_BLOCK_THRESHOLD_UNSPECIFIED,
    BLOCK_LOW_AND_ABOVE,
    BLOCK_MEDIUM_AND_ABOVE,
    BLOCK_ONLY_HIGH,
    BLOCK_NONE,
}

enum class HarmProbability {
    HARM_PROBABILITY_UNSPECIFIED,
    NEGLIGIBLE,
    LOW,
    MEDIUM,
    HIGH,
}

@Serializable
data class GenerationConfig(
    val temperature: Float?,
    @SerialName("top_p") val topP: Float?,
    @SerialName("top_k") val topK: Int?,
    @SerialName("candidate_count") val candidateCount: Int? = null,
    @SerialName("max_output_tokens") val maxOutputTokens: Int? = null,
    @SerialName("stop_sequences") val stopSequences: List<String>? = null,
    @SerialName("response_mime_type") val responseMimeType: String? = null,
    @SerialName("presence_penalty") val presencePenalty: Float? = null,
    @SerialName("frequency_penalty") val frequencyPenalty: Float? = null,
    val thinkingConfig: ThinkingConfig? = null,
)

@Serializable
data class ThinkingConfig(
    val thinkingBudget: Int? = null,
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
