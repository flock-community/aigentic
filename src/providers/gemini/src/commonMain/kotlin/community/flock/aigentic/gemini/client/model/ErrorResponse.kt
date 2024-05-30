package community.flock.aigentic.gemini.client.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: ErrorDetails,
)

@Serializable
class ErrorDetails(val code: Int, val message: String, val status: String)
