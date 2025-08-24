package community.flock.aigentic.core.agent.tool

import community.flock.aigentic.core.agent.tool.Outcome.Finished
import community.flock.aigentic.core.agent.tool.Outcome.Stuck
import community.flock.aigentic.core.tool.InternalTool
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

const val FINISHED_TASK_TOOL_NAME = "finishedTask"
const val STUCK_WITH_TASK_TOOL_NAME = "stuckWithTask"

@PublishedApi
internal inline fun <reified O : Any> finishedTaskTool(responseParameter: Parameter? = null) =
    object : InternalTool<Finished<O>> {
        val descriptionParameter =
            Parameter.Primitive(
                name = "description",
                description = "A description of the things you've done to finish the task",
                isRequired = true,
                type = ParameterType.Primitive.String,
            )

        override val name: ToolName = ToolName(FINISHED_TASK_TOOL_NAME)
        override val description: String = "When you've successfully finished the task call this function to indicate you're done."
        override val parameters = listOfNotNull(descriptionParameter, responseParameter)

        override val handler: suspend (toolArguments: JsonObject) -> Finished<O> = { arguments ->
            val description = descriptionParameter.getStringValue(arguments)
            val response =
                responseParameter?.let {
                    val json = arguments.getValue(it.name)
                    val bla = Json.decodeFromJsonElement<O>(json)
                    bla
                }
            Finished(description, response)
        }
    }

@PublishedApi
internal val stuckWithTaskTool =
    object : InternalTool<Stuck> {
        val descriptionParameter =
            Parameter.Primitive(
                name = "description",
                description = "A description of why the task can't be accomplished",
                isRequired = true,
                type = ParameterType.Primitive.String,
            )

        override val name: ToolName = ToolName(STUCK_WITH_TASK_TOOL_NAME)
        override val description: String = "When you can't accomplish the task call this function to indicate you're stuck"
        override val parameters: List<Parameter> = listOf(descriptionParameter)

        override val handler: suspend (toolArguments: JsonObject) -> Stuck = { arguments ->
            val desc = descriptionParameter.getStringValue(arguments)
            Stuck(desc)
        }
    }

sealed interface Outcome<out O : Any> {
    data class Finished<O : Any>(val description: String, val response: O?) : Outcome<O>

    data class Stuck(val reason: String) : Outcome<Nothing>

    data class Fatal(val message: String) : Outcome<Nothing>
}
