package community.flock.aigentic.vertexai

import com.fasterxml.jackson.databind.ObjectMapper

private val objectMapper = ObjectMapper()

internal fun Any.toJson(): String = objectMapper.writeValueAsString(this)

internal inline fun <reified T> String.fromJson(): T = objectMapper.readValue(this, T::class.java)
