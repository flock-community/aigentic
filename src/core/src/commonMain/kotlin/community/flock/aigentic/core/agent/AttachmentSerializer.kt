package community.flock.aigentic.core.agent

import community.flock.aigentic.core.message.MimeType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

internal const val ATTACHMENT_KEY = "aigenticAttachment"

object AttachmentSerializer : KSerializer<Attachment> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("community.flock.aigentic.core.agent.Attachment")

    override fun serialize(
        encoder: Encoder,
        value: Attachment,
    ) = encoder.asJsonEncoder().encodeJsonElement(value.toJson())

    override fun deserialize(decoder: Decoder): Attachment =
        (decoder as? JsonDecoder ?: throw SerializationException("An Attachment can only be decoded from JSON"))
            .decodeJsonElement()
            .jsonObject
            .toAttachment() ?: throw SerializationException("Not an Attachment")
}

private fun Encoder.asJsonEncoder(): JsonEncoder = this as? JsonEncoder ?: throw SerializationException("An Attachment can only be encoded to JSON")

private fun Attachment.toJson(): JsonObject =
    buildJsonObject {
        putJsonObject(ATTACHMENT_KEY) {
            when (val attachment = this@toJson) {
                is Attachment.Base64 -> {
                    put("base64Content", attachment.base64Content)
                    put("mimeType", attachment.mimeType.value)
                }

                is Attachment.Url -> {
                    put("url", attachment.url)
                    put("mimeType", attachment.mimeType.value)
                }
            }
        }
    }

internal fun JsonObject.toAttachment(): Attachment? =
    takeIf { keys == setOf(ATTACHMENT_KEY) }
        ?.getValue(ATTACHMENT_KEY)
        ?.jsonObject
        ?.let { attachment ->
            val mimeType = MimeType.entries.first { it.value == attachment.getValue("mimeType").jsonPrimitive.content }
            attachment["url"]
                ?.let { Attachment.Url(it.jsonPrimitive.content, mimeType) }
                ?: Attachment.Base64(attachment.getValue("base64Content").jsonPrimitive.content, mimeType)
        }
