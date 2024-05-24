package community.flock.aigentic.gemini.client.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

@Serializable
data class Content(val role: Role, val parts: List<Part>)

@Serializable(PartSerializer::class)
sealed interface Part {

    @Serializable
    data class Text(val text: String) : Part

    @Serializable
    data class FunctionCall(val functionCall: FunctionCallContent) : Part

    @Serializable
    data class FunctionResponse(val functionResponse: FunctionResponseContent) : Part
}

@Serializable
data class FunctionResponseContent(val name: String, val response: JsonElement)

@Serializable
data class FunctionCallContent(val name: String, val args: JsonObject)

object PartSerializer : JsonContentPolymorphicSerializer<Part>(Part::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Part> {
        val jsonObject = element.jsonObject
        return when {
            "text" in jsonObject -> Part.Text.serializer()
            "functionCall" in jsonObject -> Part.FunctionCall.serializer()
            "functionResponse" in jsonObject -> Part.FunctionResponse.serializer()
            else -> error("Unknown Part type")
        }
    }
}

