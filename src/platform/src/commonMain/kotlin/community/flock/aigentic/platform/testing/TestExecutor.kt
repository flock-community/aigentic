package community.flock.aigentic.platform.testing

import community.flock.aigentic.core.agent.Action.SendModelRequest
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.executeAction
import community.flock.aigentic.core.agent.state.State
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.ContextMessage
import community.flock.aigentic.platform.testing.exception.ExpectationFailedException
import community.flock.aigentic.platform.testing.mock.createToolMocks
import community.flock.aigentic.platform.testing.model.FailureReason
import community.flock.aigentic.platform.testing.model.TestReport
import community.flock.aigentic.platform.testing.model.TestResult
import community.flock.aigentic.platform.testing.model.message
import community.flock.aigentic.platform.testing.model.prettyPrint
import createToolCallExpectations
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll

suspend fun RegressionTest.start(): TestReport {
    return when (val configuredPlatform = agent.platform) {
        null -> aigenticException("Make sure to configure a platform in your agent")
        else -> {
            val runs = configuredPlatform.getRuns(tags)

            println("🏃 ${runs.size} runs found with tags: ${tags.joinToString(",") { it.value }}")

            val totalNumberOfTests = runs.size * numberOfIterations
            var testCounter = 1

            val testResults =
                (1..numberOfIterations).flatMap { iteration ->
                    runs.map { (runId, run) ->
                        println("🚀 [$testCounter/$totalNumberOfTests]: Starting test iteration: $iteration for run ${runId.value}")
                        executeTest(run, runId, iteration).also { testCounter++ }
                    }
                }

            TestReport.from(testResults).also(TestReport::prettyPrint)
        }
    }
}

private suspend fun RegressionTest.executeTest(
    run: Run,
    runId: RunId,
    iteration: Int,
): TestResult {
    val toolCallExpectations = createToolCallExpectations(toolCallOverrides, run)
    val toolMocks = createToolMocks(agent, toolCallExpectations)
    val mockedAgent = agent.copy(tools = toolMocks)

    return try {
        val initializedState = initializeTestState(run)
        val (resultState, result) = executeAction(SendModelRequest(initializedState, mockedAgent))
        when (result) {
            is Result.Finished -> {
                val unInvokedMocks =
                    toolMocks.filter { (name, mock) ->
                        mock.invocations.size != mock.expectations.size
                    }.mapValues {
                        it.value.expectations
                    }

                if (unInvokedMocks.isNotEmpty()) {
                    TestResult.Failed(runId, iteration, FailureReason.NotCalled(unInvokedMocks))
                } else {
                    val toolInvocations = toolMocks.mapValues { it.value.invocations }
                    TestResult.Success(runId, iteration, toolInvocations, resultState)
                }
            }

            is Result.Fatal -> TestResult.AgentError(runId, iteration, result.message)

            is Result.Stuck -> TestResult.AgentError(runId, iteration, result.reason)
        }.also {
            println(it.message())
        }
    } catch (e: ExpectationFailedException) {
        TestResult.Failed(
            runId = runId,
            iteration = iteration,
            reason =
                FailureReason.WrongArguments(
                    toolName = e.toolName,
                    expectations = e.expectations,
                    actual = e.actual,
                ),
        ).also { println(it.message()) }
    }
}

private suspend fun RegressionTest.initializeTestState(run: Run): State {
    val systemPrompt = agent.systemPromptBuilder.buildSystemPrompt(agent)
    val contextMessages = run.messages.filter { it is ContextMessage }
    val initialMessages = listOf(systemPrompt) + contextMessageInterceptor(contextMessages)
    val state = State().apply { messages.emitAll(initialMessages.asFlow()) }
    return state
}
