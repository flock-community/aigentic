package community.flock.aigentic.cloud.google.function.http

import kotlinx.serialization.json.JsonElement

data class Request(
    val method: String,
    val headers: Map<String, String>,
    val query: Map<String, String>,
    val body: JsonElement,
)
