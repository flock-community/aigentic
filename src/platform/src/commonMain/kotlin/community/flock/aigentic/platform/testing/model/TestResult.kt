package community.flock.aigentic.platform.testing.model

import ToolCallExpectation
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.state.State
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.platform.testing.util.blueString
import community.flock.aigentic.platform.testing.util.greenString
import community.flock.aigentic.platform.testing.util.redString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

sealed interface TestResult {
    data class Success(
        val runId: RunId,
        val iteration: Int,
        val toolsExecutions: Map<ToolName, List<JsonElement>>,
        val state: State,
    ) : TestResult

    data class Failed(
        val runId: RunId,
        val iteration: Int,
        val reason: FailureReason,
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
        val tools: Map<ToolName, List<ToolCallExpectation>>,
    ) : FailureReason
}

fun TestResult.message() =
    when (this) {
        is TestResult.Success -> message()
        is TestResult.Failed -> message()
        is TestResult.AgentError -> message()
    }

fun TestResult.Success.message() =
    "✅ Test of ${runId.value} success!,\n${
        toolsExecutions
            .filter { it.value.isNotEmpty() }
            .map { (toolName, arguments) ->
            "Tool: ${toolName.value.blueString()} arguments:\n${
                arguments.joinToString {
                    prettyPrintJson.encodeToString(it).greenString()
                }
            }"
        }.joinToString("\n")
    }"

val prettyPrintJson = Json { prettyPrint = true }

fun TestResult.Failed.message() =
    when (reason) {
        is FailureReason.WrongArguments -> {
            "🔴 Run: ${runId.value} tool: ${reason.toolName.value.blueString()},\nExpected:\n${
                reason.expectations.joinToString {
                    val jsonElement = prettyPrintJson.parseToJsonElement(it.toolCall.arguments)
                    prettyPrintJson.encodeToString(jsonElement)
                }.greenString()
            }\nGot: \n${prettyPrintJson.encodeToString(reason.actual).redString()}"
        }

        is FailureReason.NotCalled -> {
            "🔴 Run: ${runId.value}, tools not called: ${
                reason.tools.map { (toolName, expectations) ->
                    "Tool: ${toolName.value.blueString()}, expectations: ${
                        expectations.joinToString { it.toolCall.arguments }.blueString()
                    }"
                }
            }"
        }
    }

fun TestResult.AgentError.message() = "Run: ${runId.value}, message: $message}"
