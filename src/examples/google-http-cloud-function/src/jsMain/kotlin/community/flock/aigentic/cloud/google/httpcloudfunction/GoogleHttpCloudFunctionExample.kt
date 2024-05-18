@file:OptIn(ExperimentalJsExport::class)
@file:Suppress("ktlint:standard:max-line-length")

package community.flock.aigentic.cloud.google.httpcloudfunction

import community.flock.aigentic.cloud.google.httpcloudfunction.dsl.googleHttpCloudFunction
import community.flock.aigentic.cloud.google.httpcloudfunction.util.getEnvVar
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.dsl.openAIModel
import community.flock.aigentic.model.OpenAIModelIdentifier
import kotlinx.serialization.json.JsonObject

@JsExport
fun main() {
    val getCloudMessageTool =
        object : Tool {
            override val name = ToolName("getCloudMessage")
            override val description = null
            override val parameters = emptyList<Parameter>()
            override val handler: suspend (map: JsonObject) -> String = { "Hello from Google Cloud Function 👋" }
        }

    googleHttpCloudFunction {
        agent {
            openAIModel(getEnvVar("OPENAPI_KEY"), OpenAIModelIdentifier.GPT4O)
            task("Respond with a message from the cloud") {
                addInstruction(
                    "Call the finishedOrStuck tool when finished and use the message received from getCloudMessage as description, don't put any other text in the description",
                )
            }
            addTool(getCloudMessageTool)
        }
    }
}
