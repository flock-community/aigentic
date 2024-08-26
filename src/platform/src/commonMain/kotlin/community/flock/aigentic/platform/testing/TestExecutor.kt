package community.flock.aigentic.platform.testing

import community.flock.aigentic.core.agent.Action.SendModelRequest
import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.executeAction
import community.flock.aigentic.core.agent.state.State
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.ContextMessage
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.platform.client.AigenticPlatformClient
import community.flock.aigentic.platform.testing.exception.ExpectationFailedException
import community.flock.aigentic.platform.testing.mock.ToolMock
import community.flock.aigentic.platform.testing.mock.createToolMocks
import community.flock.aigentic.platform.testing.model.TestReport
import community.flock.aigentic.platform.testing.model.TestResult
import community.flock.aigentic.platform.testing.model.message
import community.flock.aigentic.platform.testing.model.printPretty
import community.flock.aigentic.platform.testing.util.testCounter
import createToolCallExpectations
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll

suspend fun RegressionTest.start(): TestReport {
    return when (val configuredPlatform = agent.platform) {
        null -> aigenticException("Make sure to configure a platform in your agent")
        else -> {
            val platformClient = aigenticPlatformClient(configuredPlatform)
            val runs: List<Pair<String, Run>> = platformClient.getRuns(tags)

            println("🏃 ${runs.size} runs found with tags: ${tags.joinToString(",") { it.value }}")

            val testResults =
                (1..numberOfIterations).flatMap { iteration ->
                    runs.map { (runId, run) ->
                        testCounter(runs.size * numberOfIterations) { currentTestNumber ->
                            println(
                                "🚀 Starting test [$currentTestNumber/${runs.size * numberOfIterations} iteration:$iteration] for run $runId",
                            )
                            executeTest(run, runId, iteration)
                        }
                    }
                }

            TestReport.from(testResults).also(TestReport::printPretty)
        }
    }
}

private suspend fun RegressionTest.executeTest(
    run: Run,
    runId: String,
    iteration: Int,
): TestResult {
    val toolCallExpectations = createToolCallExpectations(toolCallOverrides, run)
    val toolMocks = createToolMocks(agent, toolCallExpectations)
    val mockedAgent = agent.copy(tools = toolMocks)

    return try {
        val state = initializeTestState(run)
        executeAgent(state, mockedAgent, runId, iteration, toolMocks)
    } catch (e: ExpectationFailedException) {
        TestResult.Failed(
            runId = runId,
            iteration = iteration,
            toolName = e.toolName,
            expectations = e.expectations,
            actual = e.actual,
        ).also { println(it.message()) }
    }
}

private suspend fun executeAgent(
    state: State,
    mockedAgent: Agent,
    runId: String,
    iteration: Int,
    toolMocks: Map<ToolName, ToolMock>,
): TestResult {
    val (_, testResult) = executeAction(SendModelRequest(state, mockedAgent))

    return when (testResult) {
        is Result.Finished ->
            TestResult.Success(runId, iteration, toolMocks.mapValues { it.value.invocations })
                .also { println(it.message()) }

        is Result.Fatal -> TestResult.AgentError(runId, iteration, testResult.message).also { println(it.message()) }

        is Result.Stuck -> TestResult.AgentError(runId, iteration, testResult.reason).also { println(it.message()) }
    }
}

private suspend fun RegressionTest.initializeTestState(run: Run): State {
    val systemPrompt = agent.systemPromptBuilder.buildSystemPrompt(agent)
    val contextMessages = run.messages.filter { it is ContextMessage }
    val initialMessages = listOf(systemPrompt) + contextMessageInterceptor(contextMessages)
    val state = State().apply { messages.emitAll(initialMessages.asFlow()) }
    return state
}

private fun aigenticPlatformClient(configuredPlatform: Platform) =
    AigenticPlatformClient(
        Authentication.BasicAuth(
            username = configuredPlatform.authentication.username,
            password = configuredPlatform.authentication.password,
        ),
        configuredPlatform.apiUrl,
    )
