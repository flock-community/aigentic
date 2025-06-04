package community.flock.aigentic.example

import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.annotations.AigenticResponse

@AigenticParameter
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
