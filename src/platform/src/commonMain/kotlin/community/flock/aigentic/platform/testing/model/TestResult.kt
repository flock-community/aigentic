package community.flock.aigentic.platform.testing.model

import ToolCallExpectation
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.state.State
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.platform.testing.util.blueString
import community.flock.aigentic.platform.testing.util.greenString
import community.flock.aigentic.platform.testing.util.redString
import kotlinx.serialization.json.JsonElement

sealed interface TestResult {

    data class Success(
        val runId: RunId,
        val iteration: Int,
        val toolsExecutions: Map<ToolName, List<JsonElement>>,
        val state: State
    ) : TestResult

    data class Failed(
        val runId: RunId,
        val iteration: Int,
        val reason: FailureReason
    ) : TestResult

    data class AgentError(
        val runId: RunId,
        val iteration: Int,
        val message: String,
    ) : TestResult
}

sealed interface FailureReason {

    data class WrongArguments(
        val toolName: ToolName,
        val expectations: List<ToolCallExpectation>,
        val actual: JsonElement,
    ) : FailureReason

    data class NotCalled(
        val tools: Map<ToolName, List<ToolCallExpectation>>
    ) : FailureReason
}

fun TestResult.message() = when (this) {
    is TestResult.Success -> message()
    is TestResult.Failed -> message()
    is TestResult.AgentError -> message()
}

fun TestResult.Success.message() =
    "✅ Test of ${runId.value} success!, ${toolsExecutions.map {
            (toolName, arguments) ->
        "Tool: ${toolName.value.blueString()} arguments: ${arguments.joinToString { it.greenString() }}"
    }}"

fun TestResult.Failed.message() = when(reason) {
    is FailureReason.WrongArguments -> {
        "🔴 Run: ${runId.value} tool: ${reason.toolName.value.blueString()}, expected: ${
            reason.expectations.joinToString { it.toolCall.arguments }.greenString()
        }, got: ${reason.actual.redString()}"
    }
    is FailureReason.NotCalled -> {
        "🔴 Run: ${runId.value}, tools not called: ${reason.tools.map {
                (toolName, expectations) ->
            "Tool: ${toolName.value.blueString()}, expectations: ${expectations.joinToString { it.toolCall.arguments }.blueString()}"
        }}"
    }
}


fun TestResult.AgentError.message() = "Run: ${runId.value}, message: $message}"
