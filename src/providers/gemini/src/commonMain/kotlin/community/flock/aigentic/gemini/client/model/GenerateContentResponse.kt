package community.flock.aigentic.gemini.client.model

import kotlinx.serialization.Serializable

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>,
    val usageMetadata: UsageMetadata?
)

@Serializable
class Candidate(
    val content: Content,
    val finishReason: FinishReason?
)

@Serializable
enum class FinishReason {
    /** A new and not yet supported value. */
    UNKNOWN,

    /** Reason is unspecified. */
    UNSPECIFIED,

    /** Model finished successfully and stopped. */
    STOP,

    /** Model hit the token limit. */
    MAX_TOKENS,

    /** [SafetySetting]s prevented the model from outputting content. */
    SAFETY,

    /** Model began looping. */
    RECITATION,

    /** Model stopped for another reason. */
    OTHER
}

@Serializable
data class UsageMetadata(
    val promptTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int? = null
)
