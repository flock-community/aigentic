package community.flock.aigentic.platform.testing

import community.flock.aigentic.core.agent.Action.SendModelRequest
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.executeAction
import community.flock.aigentic.core.agent.state.State
import community.flock.aigentic.core.agent.state.addConfigContextMessage
import community.flock.aigentic.core.agent.state.addSystemPromptMessage
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.ContextMessage
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.platform.getRuns
import community.flock.aigentic.platform.testing.exception.ExpectationFailedException
import community.flock.aigentic.platform.testing.mock.createToolMocks
import community.flock.aigentic.platform.testing.model.FailureReason
import community.flock.aigentic.platform.testing.model.TestReport
import community.flock.aigentic.platform.testing.model.TestResult
import community.flock.aigentic.platform.testing.model.message
import community.flock.aigentic.platform.testing.model.prettyPrint
import createToolCallExpectations

suspend inline fun <reified I : Any, reified O : Any> RegressionTest<I, O>.start(): TestReport {
    return when (val configuredPlatform = agent.platform) {
        null -> aigenticException("Make sure to configure a platform in your agent")
        else -> {
            val runs = configuredPlatform.getRuns<O>(tags)

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

suspend inline fun <reified I : Any, reified O : Any> RegressionTest<I, O>.executeTest(
    run: Run<O>,
    runId: RunId,
    iteration: Int,
): TestResult {
    val toolCallExpectations = createToolCallExpectations(toolCallOverrides, run)
    val toolMocks = createToolMocks(agent, toolCallExpectations)
    val mockedAgent = agent.copy(tools = toolMocks)

    return try {
        val initializedState = initializeTestState(run)
        val (resultState, result) = executeAction(SendModelRequest(initializedState, mockedAgent, null))
        when (result) {
            is Outcome.Finished -> {
                val unInvokedMocks =
                    toolMocks.filter { (_, mock) ->
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

            is Outcome.Fatal -> TestResult.AgentError(runId, iteration, result.message)

            is Outcome.Stuck -> TestResult.AgentError(runId, iteration, result.reason)
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

@PublishedApi
internal suspend inline fun <reified I : Any, reified O : Any> RegressionTest<I, O>.initializeTestState(run: Run<O>): State {
    val contextMessages: List<Message> =
        run.messages
            .filterIsInstance<ContextMessage>()
            .map { it as Message }
    val systemPrompt = agent.getSystemPromptMessage()
    val interceptedContextMessages = contextMessageInterceptor(contextMessages)
    val state = State()
    state.addSystemPromptMessage(systemPrompt)
    interceptedContextMessages.forEach { state.addConfigContextMessage(it) }
    return state
}
