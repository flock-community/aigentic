package community.flock.aigentic.platform.testing.exception

import ToolCallExpectation
import community.flock.aigentic.core.tool.ToolName
import kotlinx.serialization.json.JsonElement

data class ExpectationFailedException(
    val toolName: ToolName,
    val expectations: List<ToolCallExpectation>,
    val actual: JsonElement,
) : Exception("Expectation failed for tool $toolName")

fun expectationMismatch(
    toolName: ToolName,
    expectations: List<ToolCallExpectation>,
    actual: JsonElement,
): Nothing = throw ExpectationFailedException(toolName, expectations, actual)
