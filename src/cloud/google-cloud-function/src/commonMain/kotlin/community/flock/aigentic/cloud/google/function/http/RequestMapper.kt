package community.flock.aigentic.cloud.google.function.http

import community.flock.aigentic.cloud.google.function.declarations.GoogleRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@PublishedApi
internal fun GoogleRequest.map(): Request =
    Request(
        method = method,
        headers = dynamicObjectToMap(headers),
        query = dynamicObjectToMap(query),
        body = Json.parseToJsonElement(JSON.stringify(body)),
    )

private fun dynamicObjectToMap(jsObject: dynamic): Map<String, String> =
    Json.parseToJsonElement(JSON.stringify(jsObject)).jsonObject
        .mapValues { it.value.jsonPrimitive.content }
