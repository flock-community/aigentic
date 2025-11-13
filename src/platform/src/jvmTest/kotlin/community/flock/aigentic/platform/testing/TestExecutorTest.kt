@file:Suppress("ktlint:standard:max-line-length")

package community.flock.aigentic.platform.testing

import ToolCallExpectation
import community.flock.aigentic.core.agent.AgentRun
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.message.ToolResultContent
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.platform.getRuns
import community.flock.aigentic.core.tool.createTool
import community.flock.aigentic.platform.TestData.finishedTaskToolCall
import community.flock.aigentic.platform.testing.model.FailureReason
import community.flock.aigentic.platform.toModelResponse
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlin.time.Clock

@AigenticParameter
data class NewsEventInput(
    val count: Int,
)

class TestExecutorTest : DescribeSpec({

    val expectedToolCall = ToolCall(ToolCallId("1"), newsEventTool.name.value, """{"count": 10}""")

    val toolResult =
        Message.ToolResult(
            toolCallId = expectedToolCall.id,
            toolName = newsEventTool.name.value,
            response = ToolResultContent("""[]"""),
        )

    describe("TestExecutor") {

        it("should execute test successfully") {

            val platformMock =
                createMockPlatform(
                    listOf(
                        Message.ToolCalls(
                            listOf(
                                expectedToolCall,
                                finishedTaskToolCall,
                            ),
                        ),
                        toolResult,
                    ),
                )

            val modelMock =
                createMockModel(
                    listOf(
                        // Iteration 1
                        expectedToolCall,
                        finishedTaskToolCall,
                        // Iteration 2
                        expectedToolCall,
                        finishedTaskToolCall,
                    ),
                )

            val regressionTest = createRegressionTest(platformMock, modelMock, 2)

            val testReport = regressionTest.start()

            testReport.successes.size shouldBe 2
            testReport.errors.size shouldBe 0
            testReport.failures.size shouldBe 0

            coVerify(exactly = 1) { platformMock.getRuns<Unit>(any()) }
            coVerify(exactly = 4) { modelMock.sendRequest(any(), any(), any()) }
        }

        it("should fail when expected tool is not called") {

            val platformMock =
                createMockPlatform(
                    listOf(
                        Message.ToolCalls(
                            listOf(
                                expectedToolCall,
                                finishedTaskToolCall,
                            ),
                        ),
                        toolResult,
                    ),
                )

            val modelMock =
                createMockModel(
                    listOf(
                        // Model is not calling expected tool here
                        finishedTaskToolCall,
                    ),
                )

            val regressionTest = createRegressionTest(platformMock, modelMock)

            val testReport = regressionTest.start()

            testReport.successes.size shouldBe 0
            testReport.failures.size shouldBe 1
            testReport.errors.size shouldBe 0

            testReport.failures.first().reason shouldBe
                FailureReason.NotCalled(
                    mapOf(
                        newsEventTool.name to listOf(ToolCallExpectation(expectedToolCall, toolResult)),
                    ),
                )
        }

        it("should fail when expected tool is called with unexpected arguments") {

            val platformMock =
                createMockPlatform(
                    listOf(
                        Message.ToolCalls(
                            listOf(
                                expectedToolCall,
                                finishedTaskToolCall,
                            ),
                        ),
                        toolResult,
                    ),
                )

            val wrongArguments = """{"count": 20}"""
            val modelMock =
                createMockModel(
                    listOf(
                        expectedToolCall.copy(arguments = wrongArguments),
                        finishedTaskToolCall,
                    ),
                )

            val regressionTest = createRegressionTest(platformMock, modelMock)

            val testReport = regressionTest.start()

            testReport.successes.size shouldBe 0
            testReport.failures.size shouldBe 1
            testReport.errors.size shouldBe 0

            testReport.failures.first().reason shouldBe
                FailureReason.WrongArguments(
                    newsEventTool.name,
                    listOf(ToolCallExpectation(expectedToolCall, toolResult)),
                    Json.parseToJsonElement(wrongArguments),
                )
        }

        it("should use context of run to initialize test agent") {

            val textContextMessage = Message.Text(Sender.Agent, MessageCategory.EXECUTION, "Some context message")
            val base64ContextMessage = Message.Base64(Sender.Agent, MessageCategory.EXECUTION, "base64content", MimeType.PDF)

            val platformMock =
                createMockPlatform(
                    listOf(
                        textContextMessage,
                        base64ContextMessage,
                        Message.ToolCalls(
                            listOf(
                                expectedToolCall,
                                finishedTaskToolCall,
                            ),
                        ),
                        toolResult,
                    ),
                )

            val modelMock =
                createMockModel(
                    listOf(
                        expectedToolCall,
                        finishedTaskToolCall,
                    ),
                )

            val regressionTest = createRegressionTest(platformMock, modelMock)

            val testReport = regressionTest.start()

            testReport.successes.size shouldBe 1
            testReport.failures.size shouldBe 0
            testReport.errors.size shouldBe 0

            testReport.successes.first().state.messages.replayCache shouldContainAll
                listOf(
                    textContextMessage,
                    base64ContextMessage,
                )
        }

        it("should no use Message.SystemPrompt of run but of agent instead") {

            val platformMock =
                createMockPlatform(
                    listOf(
                        Message.SystemPrompt(
                            "This is an older version version of the system prompt, the agent has a newer version which should be used instead",
                        ),
                        Message.ToolCalls(
                            listOf(
                                expectedToolCall,
                                finishedTaskToolCall,
                            ),
                        ),
                        toolResult,
                    ),
                )

            val modelMock =
                createMockModel(
                    listOf(
                        expectedToolCall,
                        finishedTaskToolCall,
                    ),
                )

            val regressionTest = createRegressionTest(platformMock, modelMock)

            val testReport = regressionTest.start()

            testReport.successes.size shouldBe 1
            testReport.failures.size shouldBe 0
            testReport.errors.size shouldBe 0

            val systemPrompt = testReport.successes.first().state.messages.replayCache.filterIsInstance<Message.SystemPrompt>().first()
            systemPrompt.prompt shouldContain "Some task description"
        }
    }
})

val newsEventTool =
    createTool<NewsEventInput, String>(
        name = "getNewsEvents",
        description = null,
    ) {
        throw IllegalStateException("Tool should not be called in a regressionTest, this tool should be mocked!")
    }

fun createMockModel(responses: List<ToolCall>): Model =
    mockk {
        coEvery { sendRequest(any(), any(), any()) } returnsMany responses.toModelResponse()
    }

fun createMockPlatform(messages: List<Message>): Platform =
    mockk {
        coEvery { getRuns<Unit>(any()) } returns
            listOf(
                RunId("some-run-id") to
                    AgentRun(
                        startedAt = Clock.System.now(),
                        finishedAt = Clock.System.now(),
                        messages = messages,
                        outcome = Outcome.Finished("Finished the task", null),
                        modelRequests = emptyList(),
                    ),
            )
    }

fun createRegressionTest(
    platform: Platform,
    model: Model,
    numberOfIterations: Int = 1,
    toolCallOverrides: List<ToolCallOverride> = emptyList(),
) = RegressionTest(
    numberOfIterations = numberOfIterations,
    tags = listOf(RunTag("tag")),
    agent =
        agent {
            platform(platform)
            model(model)
            task("Some task description") {}
            addTool(newsEventTool)
        },
    toolCallOverrides = toolCallOverrides,
    contextMessageInterceptor = { it },
)
