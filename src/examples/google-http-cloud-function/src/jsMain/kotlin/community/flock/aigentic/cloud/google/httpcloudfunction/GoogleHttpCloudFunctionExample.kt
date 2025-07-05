@file:OptIn(ExperimentalJsExport::class)

package community.flock.aigentic.cloud.google.httpcloudfunction

import community.flock.aigentic.cloud.google.function.http.dsl.Authentication.AuthorizationHeader
import community.flock.aigentic.cloud.google.function.http.dsl.googleHttpCloudFunction
import community.flock.aigentic.cloud.google.function.util.getEnvVar
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.annotations.Description
import community.flock.aigentic.openai.dsl.openAIModel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
import kotlinx.serialization.json.jsonObject

@AigenticParameter
data class TaskResponse(
    @Description("this parameter should contain the message that was send")
    val message: String,
)

@AigenticParameter
data class GreetResponse(
    val result: String,
)

@JsExport
fun main() {
    googleHttpCloudFunction<Unit, TaskResponse> {
        authentication(AuthorizationHeader("some-secret-key"))
        agent { request ->
            openAIModel {
                apiKey(getEnvVar("OPENAI_KEY"))
                modelIdentifier(OpenAIModelIdentifier.GPT4OMini)
            }
            task("Greet the person with a warm and welcome message") {}
            addTool("greet") { message: String ->
                println("Greeting message sent")
                GreetResponse("Greeting message sent")
            }
            context {
                addText("Person to greet: '${request.body.jsonObject["name"] ?: "Error: Person to greet not found"}'")
            }
        }
    }
}
