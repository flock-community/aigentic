package community.flock.aigentic.example

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import kotlinx.serialization.json.JsonObject

val saveItemTool =
    object : Tool {
        val itemName =
            Parameter.Primitive(
                "itemName",
                "The name of the item to save",
                true,
                Primitive.String,
            )

        override val name = ToolName("saveItem")
        override val description = "Saves an item"
        override val parameters = listOf(itemName)

        override val handler: suspend (JsonObject) -> String = { arguments ->

            val itemName = itemName.getStringValue(arguments)
            "Successfully saved $itemName"
        }
    }

suspend fun runItemCategorizeExample(
    apiKey: String,
    base64Image: String,
) {
    val run =
        agent {
            geminiModel(apiKey, GeminiModelIdentifier.Gemini1_5FlashLatest)
//        openAIModel(apiKey, OpenAIModelIdentifier.GPT4O)
            task("Identify all items in the image and save each individual item") {}
            addTool(saveItemTool)
            context {
                addImageBase64(base64Image, MimeType.JPEG)
            }
        }.start()

    run.messages.size
}
