package community.flock.aigentic.core.agent.tool

import community.flock.aigentic.core.tool.InternalTool
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

const val FINISH_OR_STUCK_TOOL_NAME = "finishedOrStuck"

internal fun getFinishOrStuckTool(responseParameter: Parameter? = null): InternalTool<FinishedOrStuck> {
    return object : InternalTool<FinishedOrStuck> {
        private val finishReasonParameter =
            Parameter.Complex.Enum(
                name = "finishReason",
                description = null,
                isRequired = true,
                default = null,
                values = FinishReason.getAllValues().map { PrimitiveValue.String.fromString(it::class.simpleName!!) },
                valueType = ParameterType.Primitive.String,
            )

        private val descriptionParameter =
            Parameter.Primitive(
                name = "description",
                description = "Depending on the finish reason a description of the executed work or a description of why you're stuck",
                isRequired = true,
                type = ParameterType.Primitive.String,
            )

        override val name = ToolName(FINISH_OR_STUCK_TOOL_NAME)
        override val description =
            """
            |When you've finished all tasks and met the finish condition OR when you are stuck call this tool.
            |In the case you've finished all tasks please provide a description of the work which has been done.
            |In case you're stuck please provide a description of the problem.
            """.trimMargin()

        override val parameters
            get() =
                listOf(finishReasonParameter, descriptionParameter).let {
                    if (responseParameter != null) {
                        it + responseParameter!!
                    } else {
                        it
                    }
                }
        override val handler: suspend (map: JsonObject) -> FinishedOrStuck = { arguments ->

            val stringValue = finishReasonParameter.getStringValue(arguments)
            val finishReason = FinishReason.getAllValues().first { it::class.simpleName == stringValue }
            val description = descriptionParameter.getStringValue(arguments)
            val response = responseParameter?.let { Json.encodeToString(arguments.getValue(it.name)) }
            FinishedOrStuck(finishReason, description, response)
        }
    }
}

data class FinishedOrStuck(val reason: FinishReason, val description: String, val response: String? = null)

sealed interface FinishReason {
    data object FinishedTask : FinishReason
    data object ImStuck : FinishReason

    companion object {
        fun getAllValues(): List<FinishReason> {
            return listOf(FinishedTask, ImStuck)
        }
    }
}
