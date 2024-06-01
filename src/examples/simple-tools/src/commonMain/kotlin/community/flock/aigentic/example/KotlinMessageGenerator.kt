package community.flock.aigentic.example

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
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

suspend fun runKotlinMessageAgentExample(apiKey: String) {
    agent {
        geminiModel(apiKey, GeminiModelIdentifier.Gemini1_5ProLatest)
        task("Send 5 different nice messages about Kotlin") {
            addInstruction("use the sendMessage tool 5 times")
        }
        addTool(sendMessageTool)
    }.start()
}
