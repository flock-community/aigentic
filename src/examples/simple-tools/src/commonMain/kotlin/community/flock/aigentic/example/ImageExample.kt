package community.flock.aigentic.example

import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.openai.dsl.openAIModel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
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
    base64Image: String,
    apiKey: String,
) {
    val run: Run =
        agent {
            openAIModel {
                apiKey(apiKey)
                modelIdentifier(OpenAIModelIdentifier.GPT4OMini)
            }
            task("Identify all items in the image and save each individual item") {}
            addTool(saveItemTool)
            context {
                addBase64(base64Image, MimeType.JPEG)
            }
        }.start()

    when (val result = run.result) {
        is community.flock.aigentic.core.agent.tool.Result.Finished -> "Agent finished successfully"
        is community.flock.aigentic.core.agent.tool.Result.Stuck -> "Agent is stuck and could not complete task, it says: ${result.reason}"
        is community.flock.aigentic.core.agent.tool.Result.Fatal -> "Agent crashed: ${result.message}"
    }.also(::println)
}
