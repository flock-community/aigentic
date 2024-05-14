package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.message.SystemPromptBuilder
import community.flock.aigentic.core.agent.test.util.TestData.finishedSuccessfully
import community.flock.aigentic.core.agent.test.util.TestData.modelFinishDirectly
import community.flock.aigentic.core.agent.test.util.encode
import community.flock.aigentic.core.agent.test.util.toModelResponse
import community.flock.aigentic.core.agent.tool.FinishReason
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.message.ToolResultContent
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
                            finishedSuccessfully,
                        ).toModelResponse()
                }

            agent {
                model(modelMock)
                task("Summarize the retrieved news events") {
                    addInstruction("Fetch top 10 news events")
                    addInstruction("Summarize the results")
                }
                addTool(newsEventTool)
            }.start().apply {

                result.reason shouldBe FinishReason.FinishedAllTasks
                result.description shouldBe "Finished all tasks"

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

            agent.start().apply {
                messages[0] shouldBe expectedSystemPrompt
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

            agent.start().apply {
                messages.drop(1).take(2) shouldBe
                    listOf(
                        Message.Text(Sender.Aigentic, expectedTextContext),
                        Message.Image(Sender.Aigentic, expectedImageContext),
                    )
            }
        }

        it("should add ToolCall and ToolResult to messages") {
            val toolCall = ToolCall(ToolCallId("1"), "toolName", "{}")

            val modelMock =
                mockk<Model>().apply {
                    coEvery { sendRequest(any(), any()) } returnsMany
                        listOf(
                            toolCall,
                            finishedSuccessfully,
                        ).toModelResponse()
                }

            val testTool =
                object : Tool {
                    override val name = ToolName(toolCall.name)
                    override val description = null
                    override val parameters = emptyList<Primitive>()
                    override val handler: suspend (map: JsonObject) -> String = { "toolResult" }
                }

            val agent =
                agent {
                    model(modelMock)
                    task("Execute some task") {}
                    addTool(testTool)
                }

            agent.start().apply {
                messages[1] shouldBe Message.ToolCalls(listOf(toolCall))
                messages[2] shouldBe
                    Message.ToolResult(
                        toolCallId = toolCall.id,
                        toolName = testTool.name.value,
                        response = ToolResultContent("toolResult"),
                    )
            }
        }
    }

    describe("exceptions") {

        // Implement more in https://aigentic.youtrack.cloud/issue/AIGENTIC-29/Improve-Aigentic-client-Add-error-handling
    }
})

@Serializable
data class NewsEvent(val id: Int, val title: String)
