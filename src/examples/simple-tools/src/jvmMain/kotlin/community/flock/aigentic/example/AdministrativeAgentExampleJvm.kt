package community.flock.aigentic.example

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject

// private val openAIAPIKey =
//    System.getenv("OPENAI_KEY").also {
//        if (it.isNullOrEmpty()) error("Set 'OPENAI_KEY' environment variable!")
//    }

private val geminiKey =
    System.getenv("GEMINI_API_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'GEMINI_API_KEY' environment variable!")
    }

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
            "Sent: '$message' successful"
        }
    }

fun main(): Unit =
    runBlocking {
        runAdministrativeAgentExample(geminiKey)

//        agent {
//            geminiModel(geminiKey, GeminiModelIdentifier.Gemini1_5ProLatest)
//            task("Send a nice message about Kotlin") {
//                addInstruction("use the sendMessage tool")
//            }
//            addTool(sendMessageTool)
//        }.start()
    }
