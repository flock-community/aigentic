package community.flock.aigentic.platform.testing.model

import ToolCallExpectation
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.platform.testing.util.greenString
import community.flock.aigentic.platform.testing.util.redString
import kotlinx.serialization.json.JsonElement

sealed interface TestResult {
    data class Success(
        val runId: String,
        val iteration: Int,
        val toolsExecutions: Map<ToolName, List<JsonElement>>,
    ) : TestResult

    data class Failed(
        val runId: String,
        val iteration: Int,
        val toolName: ToolName,
        val expectations: List<ToolCallExpectation>,
        val actual: JsonElement,
    ) : TestResult

    data class AgentError(
        val runId: String,
        val iteration: Int,
        val message: String,
    ) : TestResult
}

fun TestResult.Success.message() =
    "✅ Test of $runId success!, ${toolsExecutions.map {
            (toolName, arguments) ->
        "Tool: ${toolName.value} executions: ${arguments.joinToString { it.greenString() }}"
    }}"

fun TestResult.Failed.message() =
    "Run: $runId tool: ${toolName.value}, expected: ${
        expectations.joinToString { it.toolCall.arguments }.greenString()
    }, got: ${actual.redString()}"

fun TestResult.AgentError.message() = "Run: $runId, message: $message}"
