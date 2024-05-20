package community.flock.aigentic.cloud.google.function.http

import kotlinx.serialization.json.JsonObject

data class Request(
    val method: String,
    val headers: Map<String, String>,
    val query: Map<String, String>,
    val body: JsonObject,
)
