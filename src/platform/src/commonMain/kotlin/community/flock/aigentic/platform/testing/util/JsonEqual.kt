package community.flock.aigentic.platform.testing.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun JsonArray.equalsIgnoreOrder(other: JsonArray): Boolean {
    if (this.size != other.size) return false
    return this.all { item -> other.any { it.jsonEquals(item) } }
}

fun JsonElement.jsonEquals(other: JsonElement): Boolean {
    return when {
        this is JsonPrimitive && other is JsonPrimitive -> this == other
        this is JsonArray && other is JsonArray -> this.equalsIgnoreOrder(other)
        this is JsonObject && other is JsonObject ->
            this.keys == other.keys &&
                this.all { (key, value) ->
                    other[key]?.jsonEquals(value) == true
                }

        else -> false
    }
}
