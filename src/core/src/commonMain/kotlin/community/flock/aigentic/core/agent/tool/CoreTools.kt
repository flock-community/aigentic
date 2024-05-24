package community.flock.aigentic.core.agent.tool

import community.flock.aigentic.core.agent.tool.Result.Finished
import community.flock.aigentic.core.agent.tool.Result.Stuck
import community.flock.aigentic.core.tool.InternalTool
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
import kotlinx.serialization.json.JsonObject

internal val finishedTaskTool = object : InternalTool<Finished> {

    val descriptionParameter =
        Parameter.Primitive(
            name = "description",
            description = "A description of the things you've done to finish the task",
            isRequired = true,
            type = ParameterType.Primitive.String,
        )

    override val name: ToolName = ToolName("finishedTask")
    override val description: String = "When you've successfully finished the task call this function to indicate you're done."
    override val parameters: List<Parameter> = listOf(descriptionParameter)

    override val handler: suspend (map: JsonObject) -> Finished = { arguments ->

        val desc = descriptionParameter.getStringValue(arguments)
        Finished(desc)
    }
}

internal val stuckWithTaskTool = object : InternalTool<Stuck> {

    val descriptionParameter =
        Parameter.Primitive(
            name = "description",
            description = "A description of why the task can't be accomplished",
            isRequired = true,
            type = ParameterType.Primitive.String,
        )

    override val name: ToolName = ToolName("stuckWithTask")
    override val description: String = "When you can't accomplish the task call this function to indicate you're stuck"
    override val parameters: List<Parameter> = listOf(descriptionParameter)

    override val handler: suspend (map: JsonObject) -> Stuck = { arguments ->

        val desc = descriptionParameter.getStringValue(arguments)
        Stuck(desc)
    }
}

sealed interface Result {
    data class Finished(val description: String) : Result
    data class Stuck(val description: String): Result
    data class Fatal(val message: String): Result
}



sealed interface FinishReason {
    data object FinishedTask : FinishReason
    data object ImStuck : FinishReason

    companion object {
        fun getAllValues(): List<FinishReason> {
            return listOf(FinishedTask, ImStuck)
        }
    }
}
