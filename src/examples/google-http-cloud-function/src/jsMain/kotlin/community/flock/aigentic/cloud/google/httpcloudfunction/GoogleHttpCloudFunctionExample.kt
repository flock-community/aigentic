@file:OptIn(ExperimentalJsExport::class)
@file:Suppress("ktlint:standard:max-line-length")

package community.flock.aigentic.cloud.google.httpcloudfunction

import community.flock.aigentic.cloud.google.function.http.dsl.Authentication.AuthorizationHeader
import community.flock.aigentic.cloud.google.function.http.dsl.googleHttpCloudFunction
import community.flock.aigentic.cloud.google.function.util.getEnvVar
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.Parameter.Complex.Object
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
import community.flock.aigentic.openai.dsl.openAIModel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

@JsExport
fun main() {
    val greetTool =
        object : Tool {
            val messageParameter =
                Parameter.Primitive(
                    "greetingMessage",
                    "The message to greet the person with",
                    true,
                    Primitive.String,
                )

            override val name = ToolName("greet")
            override val description = null
            override val parameters = listOf(messageParameter)
            override val handler: suspend (toolArguments: JsonObject) -> String = { arguments ->

                val message = messageParameter.getStringValue(arguments)
                println(message)
                "greeting message sent"
            }
        }

    val responseParameter =
        Object(
            name = "ResponseObject",
            description = "This object is the response of the agent when finished",
            isRequired = true,
            listOf(
                Parameter.Primitive(
                    name = "message",
                    description = "this parameter should contain the message that was send",
                    isRequired = true,
                    Primitive.String,
                ),
            ),
        )

    googleHttpCloudFunction {
        authentication(AuthorizationHeader("some-secret-key"))
        agent { request ->
            openAIModel(getEnvVar("OPENAI_KEY"), OpenAIModelIdentifier.GPT4O)
            task("Greet the person with a warm and welcome message") {}
            addTool(greetTool)
            context {
                addText("Person to greet: '${ request.body.jsonObject["name"] ?: "Error: Person to greet not found"}'")
            }
            finishResponse(responseParameter)
        }
    }
}
