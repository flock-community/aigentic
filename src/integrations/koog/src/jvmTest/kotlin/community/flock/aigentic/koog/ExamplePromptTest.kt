package community.flock.aigentic.koog

import community.flock.aigentic.core.agent.AgentRun
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.message.ToolResultContent
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.platform.PlatformClient
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import ai.koog.prompt.message.Message as KoogMessage

class ExamplePromptTest :
    DescribeSpec({

        describe("fetchExampleRunPrompt") {

            it("only sets the system prompt when no tags are given") {
                val platform = mockk<Platform>()

                val (prompt, exampleRunIds) = fetchExampleRunPrompt<String>(platform, emptyList(), "You are a helpful agent")

                prompt.messages.filterIsInstance<KoogMessage.System>().map { it.textContent() } shouldBe listOf("You are a helpful agent")
                prompt.messages.filterIsInstance<KoogMessage.User>() shouldBe emptyList()
                exampleRunIds shouldBe emptyList()
            }

            it("only sets the system prompt when no runs match the tags") {
                val client =
                    mockk<PlatformClient>().apply {
                        coEvery { getRuns(listOf(RunTag("example"))) } returns emptyList()
                    }
                val platform = mockk<Platform>().apply { every { this@apply.client } returns client }

                val (prompt, exampleRunIds) = fetchExampleRunPrompt<String>(platform, listOf(RunTag("example")), "You are a helpful agent")

                prompt.messages.filterIsInstance<KoogMessage.System>().map { it.textContent() } shouldBe listOf("You are a helpful agent")
                prompt.messages.filterIsInstance<KoogMessage.User>() shouldBe emptyList()
                exampleRunIds shouldBe emptyList()
            }

            it("splices example run messages in as user turns, wrapped in start/end markers") {
                val now = Clock.System.now()
                val exampleRun =
                    AgentRun(
                        startedAt = now,
                        finishedAt = now,
                        messages =
                            listOf(
                                Message.Text(Sender.Agent, "What's the weather in Amsterdam?", MessageCategory.RUN_CONTEXT),
                                Message.ToolCalls(listOf(ToolCall(ToolCallId("1"), "getWeather", """{"city":"Amsterdam"}"""))),
                                Message.ToolResult(ToolCallId("1"), "getWeather", ToolResultContent("It's sunny and 21°C")),
                            ),
                        outcome = Outcome.Finished("done", Json.encodeToString("It's sunny in Amsterdam")),
                        modelRequests = emptyList(),
                        systemPromptMessage = Message.SystemPrompt("You are a helpful agent"),
                    )

                val client =
                    mockk<PlatformClient>().apply {
                        coEvery { getRuns(listOf(RunTag("example"))) } returns listOf(RunId("run-1") to exampleRun)
                    }
                val platform = mockk<Platform>().apply { every { this@apply.client } returns client }

                val (prompt, exampleRunIds) = fetchExampleRunPrompt<String>(platform, listOf(RunTag("example")), "You are a helpful agent")

                val userTexts = prompt.messages.filterIsInstance<KoogMessage.User>().map { it.textContent() }

                userTexts shouldBe
                    listOf(
                        "The messages below are example run messages. Each example run has a clearly marked start and end.\n" +
                            "The example messages continue until you encounter: <END_OF_ALL_EXAMPLES>",
                        "The messages below are part of example 0. The example ends when you encounter: <END_EXAMPLE_0>",
                        "What's the weather in Amsterdam?",
                        "Tool call with arguments: " + """{"city":"Amsterdam"}""",
                        "Tool call result: It's sunny and 21°C",
                        "<END_EXAMPLE_0>.",
                        "<END_OF_ALL_EXAMPLES>\n" +
                            "All of the previous example messages are to be considered as the results of a desired run.\n" +
                            "Carefully analyze the relationship between the input (instructions, tool calls and arguments) and the output (responses).\n" +
                            "Use these relations in the current task and make sure to apply the instructions below to come to the same relationships.\n" +
                            "All messages following are the input for the current task.",
                    )

                (prompt.messages.first() as KoogMessage.System).textContent() shouldBe "You are a helpful agent"
                exampleRunIds shouldBe listOf(RunId("run-1"))
            }
        }
    })
