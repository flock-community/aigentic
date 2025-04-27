package community.flock.aigentic.example

import community.flock.aigentic.code.generation.annotations.AigenticParameter
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.TypedTool
import community.flock.aigentic.core.tool.getStringValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@AigenticParameter
@Serializable
data class KotlinMessage(val message: String)

val sendMessageTool = object : TypedTool<KotlinMessage, String> {
    override val name = ToolName("sendMessageTool")
    override val description = "Optional description of the tool."
    override val parameters = emptyList<Parameter>()
    override val handler: suspend (toolArguments: KotlinMessage) -> String = {
        "Sent: ${it.message}"
    }
}

data class MessageToolResult(val result: String)

suspend fun runKotlinMessageAgentExample(configureModel: AgentConfig.() -> Unit): Run {
    val run =
        agent {
            configureModel()
            task("Send 2 nice messages about Kotlin") {
                addInstruction("use the sendMessage tool to send an individual message")
                addInstruction("After the message has been send you're finished")
            }
            addTool(sendMessageTool)
        }.start()

    return run
}
