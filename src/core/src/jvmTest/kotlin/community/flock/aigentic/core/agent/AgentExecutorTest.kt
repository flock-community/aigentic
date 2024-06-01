package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.message.SystemPromptBuilder
import community.flock.aigentic.core.agent.test.util.TestData.finishedTaskToolCall
import community.flock.aigentic.core.agent.test.util.TestData.modelFinishTaskDirectly
import community.flock.aigentic.core.agent.test.util.TestData.modelStuckDirectly
import community.flock.aigentic.core.agent.test.util.encode
import community.flock.aigentic.core.agent.test.util.toModelResponse
import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import community.flock.aigentic.core.agent.tool.Result.Fatal
import community.flock.aigentic.core.agent.tool.Result.Finished
import community.flock.aigentic.core.agent.tool.Result.Stuck
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.exception.AigenticException
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.message.ToolResultContent
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.Parameter.Primitive
import community.flock.aigentic.core.tool.ParameterType.Primitive.Integer
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeTypeOf
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
                mockk<suspend (toolArguments: JsonObject) -> String>().apply {
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
                    override val handler: suspend (toolArguments: JsonObject) -> String = mockHandler
                }

            val expectedArguments = buildJsonObject { put("count", 10) }

            val modelMock =
                mockk<Model>().apply {
                    coEvery { sendRequest(any(), any()) } returnsMany
                        listOf(
                            ToolCall(ToolCallId("1"), newsEventTool.name.value, expectedArguments.encode()),
                            finishedTaskToolCall,
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

                result.shouldBeInstanceOf<Finished>()
                (result as Finished).description shouldBe "Finished the task"

                coVerify(exactly = 1) { mockHandler.invoke(expectedArguments) }
                coVerify(exactly = 2) { modelMock.sendRequest(any(), any()) }
            }
        }

        it("should finish with Stuck result when model doesn't know what to do") {

            agent {
                model(modelStuckDirectly)
                task("Summarize the retrieved news events") {
                    addInstruction("Fetch top 10 news events")
                }
                addTool(mockk<Tool>(relaxed = true))
            }.start().apply {
                result.shouldBeInstanceOf<Stuck>()
                (result as Stuck).description shouldBe "I don't know what to do"
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
                    model(modelFinishTaskDirectly)
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
            val expectedImageContextBase64 = "base-64-encoded-string"
            val expectedImageContextMimeType = MimeType.PNG

            val agent =
                agent {
                    model(modelFinishTaskDirectly)
                    task("Execute some task") {}
                    context {
                        addText(expectedTextContext)
                        addImageBase64(base64 = expectedImageContextBase64, mimeType = expectedImageContextMimeType)
                    }
                    addTool(mockk(relaxed = true))
                }

            agent.start().apply {
                messages.drop(1).take(2) shouldBe
                    listOf(
                        Message.Text(Sender.Aigentic, expectedTextContext),
                        Message.ImageBase64(
                            sender = Sender.Aigentic,
                            base64Content = expectedImageContextBase64,
                            mimeType = expectedImageContextMimeType,
                        ),
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
                            finishedTaskToolCall,
                        ).toModelResponse()
                }

            val testTool =
                object : Tool {
                    override val name = ToolName(toolCall.name)
                    override val description = null
                    override val parameters = emptyList<Primitive>()
                    override val handler: suspend (toolArguments: JsonObject) -> String = { "toolResult" }
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

        it("if finishedWith parameter is configured, its result should be in the finished response field") {
            val parameterName = "response"
            val response = buildJsonObject { put("message", "Agent response") }
            val finishParameter =
                Parameter.Complex.Object(
                    name = parameterName,
                    description = "some description",
                    true,
                    parameters = emptyList(),
                )
            val toolCall =
                ToolCall(
                    ToolCallId("1"),
                    FINISHED_TASK_TOOL_NAME,
                    buildJsonObject {
                        put("description", "Finished the task")
                        put(parameterName, response)
                    }.encode(),
                )
            val modelMock =
                mockk<Model>().apply {
                    coEvery { sendRequest(any(), any()) } returnsMany
                        listOf(
                            toolCall,
                        ).toModelResponse()
                }
            val agent =
                agent {
                    model(modelMock)
                    task("Execute some task") {}
                    addTool(mockk(relaxed = true))
                    finishResponse(finishParameter)
                }

            agent.start().apply {
                result.shouldBeTypeOf<Finished>()
                (this.result as Finished).response shouldBe response.encode()
            }
        }

        it("if finishedWith parameter is not configured, the finished response field should be null") {
            val agent =
                agent {
                    model(modelFinishTaskDirectly)
                    task("Execute some task") {}
                    addTool(mockk(relaxed = true))
                }

            agent.start().apply {
                result.shouldBeTypeOf<Finished>()
                (this.result as Finished).response shouldBe null
            }
        }
    }

    describe("exceptions") {

        it("should finish with Fatal result when model throws AigenticException") {

            val modelMock =
                mockk<Model>().apply {
                    coEvery { sendRequest(any(), any()) } throws AigenticException("Model exception")
                }

            agent {
                model(modelMock)
                task("Summarize the retrieved news events") {
                    addInstruction("Fetch top 10 news events")
                }
                addTool(mockk<Tool>(relaxed = true))
            }.start().apply {
                result.shouldBeInstanceOf<Fatal>()
                (result as Fatal).message shouldBe "Model exception"
            }
        }

        // Implement more in https://aigentic.youtrack.cloud/issue/AIGENTIC-29/Improve-Aigentic-client-Add-error-handling
    }
})

@Serializable
data class NewsEvent(val id: Int, val title: String)
