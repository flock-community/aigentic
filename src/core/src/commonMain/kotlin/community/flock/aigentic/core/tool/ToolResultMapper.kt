package community.flock.aigentic.core.tool

import community.flock.aigentic.core.agent.Attachment
import community.flock.aigentic.core.agent.toAttachment
import community.flock.aigentic.core.message.ToolResultContent
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@PublishedApi
internal fun JsonElement.toToolResultContent(): ToolResultContent =
    replaceAttachments(emptyList()).let { (json, attachments) ->
        ToolResultContent(json.toString(), attachments)
    }

private fun JsonElement.replaceAttachments(found: List<Attachment>): Pair<JsonElement, List<Attachment>> =
    when (this) {
        is JsonObject -> {
            toAttachment()
                ?.let { (found + it).let { attachments -> JsonPrimitive("[attachment ${attachments.size}]") to attachments } }
                ?: entries
                    .fold(emptyMap<String, JsonElement>() to found) { (properties, attachments), (key, value) ->
                        value.replaceAttachments(attachments).let { (json, all) -> properties + (key to json) to all }
                    }.let { (properties, attachments) -> JsonObject(properties) to attachments }
        }

        is JsonArray -> {
            fold(emptyList<JsonElement>() to found) { (elements, attachments), value ->
                value.replaceAttachments(attachments).let { (json, all) -> elements + json to all }
            }.let { (elements, attachments) -> JsonArray(elements) to attachments }
        }

        else -> {
            this to found
        }
    }
