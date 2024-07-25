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
            platform {
                name("kotlin-message-agent")
                secret("f80f8d0c-4498-45dc-905f-34802db78e91")
            }
            model(model)
            task("Send 1 nice message about Kotlin") {
                addInstruction("use the sendMessage tool to send")
            }
            addTool(sendMessageTool)
        }.start()

    return run
}
