package community.flock.aigentic.example

import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
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
    model: Model,
    base64Image: String,
): Run {
    val run =
        agent {
            model(model)
            task("Identify all items in the image and save each individual item") {}
            addTool(saveItemTool)
            context {
                addImageBase64(base64Image, MimeType.JPEG)
            }
        }.start()

    return run
}
