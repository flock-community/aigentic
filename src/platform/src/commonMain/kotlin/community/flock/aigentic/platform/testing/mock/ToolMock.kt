package community.flock.aigentic.platform.testing.mock

import ToolCallExpectation
import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.message.argumentsAsJson
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.platform.testing.exception.expectationMismatch
import community.flock.aigentic.platform.testing.util.jsonEquals
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

data class ToolMock(
    val expectations: List<ToolCallExpectation>,
    override val name: ToolName,
    override val description: String?,
    override val parameters: List<Parameter>,
) : Tool {
    val invocations: MutableList<JsonElement> = mutableListOf()

    override val handler: suspend (toolArguments: JsonObject) -> String = { arguments ->

        invocations.add(arguments)

        val result =
            expectations
                .find { it.toolCall.argumentsAsJson().jsonEquals(arguments) }
                ?.toolResult
                ?: expectationMismatch(
                    toolName = name,
                    expectations = expectations,
                    actual = arguments,
                )

        result.response.result
    }
}

fun createToolMocks(
    agent: Agent,
    toolCallExpectations: List<ToolCallExpectation>,
) = agent.tools.mapValues { (name, value) ->

    val toolExpectations =
        toolCallExpectations
            .filter { it.toolCall.name == name.value }
            .also {
                if (it.isEmpty()) {
                    println("⚠️ No expectations found for tool $name, did you maybe rename the tool in your agent configuration?")
                }
            }

    ToolMock(
        expectations = toolExpectations,
        name = name,
        description = value.description,
        parameters = value.parameters,
    )
}
