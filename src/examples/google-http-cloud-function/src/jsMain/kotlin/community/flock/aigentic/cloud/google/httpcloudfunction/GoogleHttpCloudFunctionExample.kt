@file:OptIn(ExperimentalJsExport::class)
@file:Suppress("ktlint:standard:max-line-length")

package community.flock.aigentic.cloud.google.httpcloudfunction

import community.flock.aigentic.cloud.google.httpcloudfunction.dsl.googleHttpCloudFunction
import community.flock.aigentic.cloud.google.httpcloudfunction.util.getEnvVar
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
import community.flock.aigentic.dsl.openAIModel
import community.flock.aigentic.model.OpenAIModelIdentifier
import kotlinx.serialization.json.JsonObject


@JsExport
fun main() {

    val getCloudMessageTool =
        object : Tool {

            val nameParameter =
                Parameter.Primitive(
                    "name",
                    "The name of the person to greet",
                    true,
                    Primitive.String,
                )

            override val name = ToolName("getCloudMessage")
            override val description = null
            override val parameters = listOf(nameParameter)
            override val handler: suspend (map: JsonObject) -> String = { arguments ->

                val name = nameParameter.getStringValue(arguments)

                "$name, hello from Google Cloud Function 👋"
            }
        }

    googleHttpCloudFunction {
        agent { request ->
            openAIModel(getEnvVar("OPENAPI_KEY"), OpenAIModelIdentifier.GPT4O)
            task("Respond with a welcome message to the person") {
                addInstruction(
                    "Call the finishedOrStuck tool when finished and use the message received from getCloudMessage as description, don't put any other text in the description",
                )
            }
            addTool(getCloudMessageTool)
            context {
                addText("Person to welcome: '${request.query["name"] ?: "Person to welcome not found"}'")
            }
        }
    }
}
