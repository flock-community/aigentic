package community.flock.aigentic.core.agent.tool

import community.flock.aigentic.core.agent.tool.Result.Finished
import community.flock.aigentic.core.agent.tool.Result.Stuck
import community.flock.aigentic.core.tool.InternalTool
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

const val FINISHED_TASK_TOOL_NAME = "finishedTask"
const val STUCK_WITH_TASK_TOOL_NAME = "stuckWithTask"

internal fun finishedTaskTool(responseParameter: Parameter? = null) =
    object : InternalTool<Finished> {
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

        override val handler: suspend (toolArguments: JsonObject) -> Finished = { arguments ->

            val description = descriptionParameter.getStringValue(arguments)
            val response = responseParameter?.let { Json.encodeToString(arguments.getValue(it.name)) }
            Finished(description, response)
        }
    }

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

sealed interface Result {
    data class Finished(val description: String, val response: String?) : Result

    data class Stuck(val reason: String) : Result

    data class Fatal(val message: String) : Result
}
