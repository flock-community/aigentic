package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.prompt.SystemPromptBuilder
import community.flock.aigentic.core.agent.test.util.encode
import community.flock.aigentic.core.agent.test.util.toModelResponse
import community.flock.aigentic.core.agent.tool.FinishReason
import community.flock.aigentic.core.agent.tool.finishOrStuckTool
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.tool.Parameter.Primitive
import community.flock.aigentic.core.tool.ParameterType.Primitive.Integer
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AgentExecutorTest : DescribeSpec({

    describe("happy path") {

        it("should run agent successfully") {

            val mockHandler =
                mockk<suspend (map: JsonObject) -> String>().apply {
                    coEvery { this@apply.invoke(any<JsonObject>()) } returns
                        Json.encodeToString(
                            listOf(
                                NewsEvent(1, "News event about AI"),
                                NewsEvent(2, "News event about Aigentic"),
                            ),
                        )
                }

            val newsEventTool =
                object : Tool {
                    override val name = ToolName("getNewsEvents")
                    override val description = null
                    override val parameters =
                        listOf(
                            Primitive("count", "number of events", true, Integer),
                        )
                    override val handler: suspend (map: JsonObject) -> String = mockHandler
                }

            val expectedArguments = buildJsonObject { put("count", 10) }

            val modelMock =
                mockk<Model>().apply {
                    coEvery { sendRequest(any(), any()) } returnsMany
                        listOf(
                            ToolCall(ToolCallId("1"), newsEventTool.name.value, expectedArguments.encode()),
                            ToolCall(
                                ToolCallId("2"), finishOrStuckTool.name.value,
                                buildJsonObject {
                                    put("finishReason", "FinishedAllTasks")
                                    put("description", "There are 2 news events about Aigentic and AI")
                                }.encode(),
                            ),
                        ).toModelResponse()
                }

            agent {
                model(modelMock)
                task("Summarize the retrieved news events") {
                    addInstruction("Fetch top 10 news events")
                    addInstruction("Summarize the results")
                }
                addTool(newsEventTool)
            }.run().apply {
                reason shouldBe FinishReason.FinishedAllTasks
                description shouldBe "There are 2 news events about Aigentic and AI"

                coVerify(exactly = 1) { mockHandler.invoke(expectedArguments) }
                coVerify(exactly = 2) { modelMock.sendRequest(any(), any()) }
            }
        }

        it("should add system prompt as first message") {

            val expectedSystemPrompt = Message.SystemPrompt("You are a helpful agent")
            val systemPromptMock =
                mockk<SystemPromptBuilder>().apply {
                    every { buildSystemPrompt(any()) } returns expectedSystemPrompt
                }

            val agent =
                agent {
                    model(modelFinishDirectly)
                    systemPrompt(systemPromptMock)
                    task("Execute some task") {}
                    addTool(mockk(relaxed = true))
                }

            agent.run().apply {
                agent.messages.replayCache.first() shouldBe expectedSystemPrompt
                verify(exactly = 1) { systemPromptMock.buildSystemPrompt(any()) }
            }
        }

        it("should add provided context as messages after system prompt message") {

            val expectedTextContext = "This is some text context"
            val expectedImageContext = "base-64-encoded-string"

            val agent =
                agent {
                    model(modelFinishDirectly)
                    task("Execute some task") {}
                    context {
                        addText(expectedTextContext)
                        addImage(expectedImageContext)
                    }
                    addTool(mockk(relaxed = true))
                }

            agent.run().apply {
                agent.messages.replayCache.drop(1).take(2) shouldBe
                    listOf(
                        Message.Text(Sender.Aigentic, expectedTextContext),
                        Message.Image(Sender.Aigentic, expectedImageContext),
                    )
            }
        }
    }

    describe("exceptions") {
        // Implement in https://aigentic.youtrack.cloud/issue/AIGENTIC-29/Improve-Aigentic-client-Add-error-handling
    }
})

private val modelFinishDirectly =
    mockk<Model>().apply {
        coEvery { sendRequest(any(), any()) } returnsMany
            listOf(
                ToolCall(
                    ToolCallId("1"), finishOrStuckTool.name.value,
                    buildJsonObject {
                        put("finishReason", "FinishedAllTasks")
                        put("description", "Finished all tasks")
                    }.encode(),
                ),
            ).toModelResponse()
    }

@Serializable
data class NewsEvent(val id: Int, val title: String)
