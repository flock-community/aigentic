package community.flock.aigentic.example

import community.flock.aigentic.core.annotations.AigenticParameter

@AigenticParameter
data class WeatherRequest(
    val location: String,
    val date: String? = null,
)

@AigenticParameter
data class WeatherResponse(
    val temperature: String,
    val conditions: String,
    val location: String,
    val date: String,
)
