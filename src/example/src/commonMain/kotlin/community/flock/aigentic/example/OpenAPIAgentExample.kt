package community.flock.aigentic.example

import community.flock.aigentic.core.agent.run
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
import community.flock.aigentic.dsl.openAIModel
import community.flock.aigentic.model.OpenAIModelIdentifier
import community.flock.aigentic.tools.openapi.dsl.openApiTools
import kotlinx.serialization.json.JsonObject

suspend fun runOpenAPIAgent(openAIAPIKey: String, hackerNewsOpenAPISpec: String) {

    agent {
        openAIModel(openAIAPIKey, OpenAIModelIdentifier.GPT4Turbo)
        task("Send Hacker News stories about AI") {
            addInstruction("Retrieve the top 10 Hacker News stories")
            addInstruction("Send stories, if any, about AI to john@doe.com")
        }
        openApiTools(hackerNewsOpenAPISpec)
        addTool(sendEmailTool)
    }.run()
}

val sendEmailTool = object : Tool {

    val emailAddressParam = Parameter.Primitive(
        "emailAddress", "The recipient email address", true, Primitive.String
    )

    val subjectParam = Parameter.Primitive(
        "subject", "Email subject", true, Primitive.String
    )

    val messageParam = Parameter.Primitive(
        "message", "Email message", true, Primitive.String
    )

    override val name = ToolName("SendEmail")
    override val description = "Sends a Email to the provided recipient"
    override val parameters = listOf(emailAddressParam, subjectParam, messageParam)

    override val handler: suspend (JsonObject) -> String = { arguments ->

        val emailAddress = emailAddressParam.getStringValue(arguments)
        val subject = subjectParam.getStringValue(arguments)
        val message = messageParam.getStringValue(arguments)

        "✉️ Sending email: '$message' with subject: '$subject' to recipient: $emailAddress"
    }
}
