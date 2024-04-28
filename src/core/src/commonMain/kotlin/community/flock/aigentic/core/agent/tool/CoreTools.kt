package community.flock.aigentic.core.agent.tool

import community.flock.aigentic.core.tool.*
import kotlinx.serialization.json.JsonObject

internal val finishOrStuckTool = object : InternalTool<FinishedOrStuck> {

    val finishReasonParameter = Parameter.Complex.Enum(
        name = "finishReason",
        description = "The telephone number of the receiver of this message",
        isRequired = true,
        default = null,
        values = FinishReason.getAllValues().map { PrimitiveValue.String.fromString(it::class.simpleName!!) },
        valueType = ParameterType.Primitive.String
    )

    val descriptionParameter = Parameter.Primitive(
        name = "description",
        description = "Depending on the finish reason a description of the executed work OR a description of why you're stuck",
        isRequired = true,
        type = ParameterType.Primitive.String
    )

    override val name = ToolName("finishedOrStuck")
    override val description =
        "When you've finished all tasks and met the finish condition OR when you are stuck call this tool. In the case you've finished all tasks please provide a description of the work which has been done. In case you're stuck please provide a description of the problem. When you're stuck don't call any other functions afterwards!"
    override val parameters = listOf(finishReasonParameter, descriptionParameter)
    override val handler: suspend (map: JsonObject) -> FinishedOrStuck = { arguments ->

        val stringValue = finishReasonParameter.getStringValue(arguments)
        val finishReason = FinishReason.getAllValues().first { it::class.simpleName == stringValue }
        val description = descriptionParameter.getStringValue(arguments)
        FinishedOrStuck(finishReason, description)
    }
}

data class FinishedOrStuck(val reason: FinishReason, val description: String)

sealed interface FinishReason  {
    data object FinishedAllTasks : FinishReason
    data object ImStuck : FinishReason

    companion object {
        fun getAllValues(): List<FinishReason> {
            return listOf(FinishedAllTasks, ImStuck)
        }
    }
}
