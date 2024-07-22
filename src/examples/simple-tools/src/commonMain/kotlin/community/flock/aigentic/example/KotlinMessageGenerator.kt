package community.flock.aigentic.example

import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
import community.flock.aigentic.platform.dsl.platform
import kotlinx.serialization.json.JsonObject

val sendMessageTool =
    object : Tool {
        val messageParam =
            Parameter.Primitive(
                "message",
                null,
                true,
                Primitive.String,
            )

        override val name = ToolName("sendMessage")
        override val description = "Sends a message"
        override val parameters = listOf(messageParam)

        override val handler: suspend (JsonObject) -> String = { arguments ->

            val message = messageParam.getStringValue(arguments)
            "Successfully sent: '$message' "
        }
    }

suspend fun runKotlinMessageAgentExample(model: Model): Run {
    val run =
        agent {
            name("kotlin-message-agent")
            platform {
                name("kotlin-message-agent")
                secret("e643793b-8557-4ed4-8634-b6d00a72ade0")
                apiUrl("http://localhost:8080")
            }
            model(model)
            task("Send 3 nice message about Kotlin") {
                addInstruction("use the sendMessage tool to send")
            }
            addTool(sendMessageTool)
        }.start()

    return run
}
