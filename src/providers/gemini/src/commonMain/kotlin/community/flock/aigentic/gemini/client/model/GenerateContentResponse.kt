package community.flock.aigentic.gemini.client.model

import kotlinx.serialization.Serializable

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null,
    val usageMetadata: UsageMetadata?,
)

@Serializable
data class Candidate(
    val content: Content? = null,
    val finishReason: FinishReason? = null,
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

    /** The model produced a malformed function call. */
    MALFORMED_FUNCTION_CALL,

    /** Model stopped for another reason. */
    OTHER,
}

@Serializable
data class UsageMetadata(
    val promptTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int? = null,
    val thoughtsTokenCount: Int? = null,
    val cachedContentTokenCount: Int? = null,
)

@Serializable
data class PromptFeedback(
    val blockReason: BlockReason? = null,
    val safetyRatings: List<SafetyRating>? = null,
)

@Serializable
enum class BlockReason {
    BLOCK_REASON_UNSPECIFIED,
    SAFETY,
    OTHER,
    BLOCKLIST,
    PROHIBITED_CONTENT,
    IMAGE_SAFETY,
}

@Serializable
data class SafetyRating(
    val category: HarmCategory,
    val probability: HarmProbability,
    val blocked: Boolean,
)
