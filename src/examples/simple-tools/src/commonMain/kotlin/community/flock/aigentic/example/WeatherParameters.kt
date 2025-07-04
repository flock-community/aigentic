package community.flock.aigentic.example

import community.flock.aigentic.core.annotations.AigenticResponse
import kotlinx.serialization.Serializable

@Serializable
data class WeatherRequest(
    val location: String,
    val date: String? = null,
)

@AigenticResponse
data class WeatherResponse(
    val temperature: String,
    val conditions: String,
    val location: String,
    val date: String,
)
