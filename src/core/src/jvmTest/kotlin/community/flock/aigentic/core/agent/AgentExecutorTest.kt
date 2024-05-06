package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.test.util.encode
import community.flock.aigentic.core.agent.test.util.toModelResponse
import community.flock.aigentic.core.agent.tool.FinishReason
import community.flock.aigentic.core.agent.tool.finishOrStuckTool
import community.flock.aigentic.core.dsl.agent
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
import io.mockk.mockk
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AgentExecutorTest : DescribeSpec({

    describe("happy path") {

        it("should run agent successfully") {

            val mockHandler = mockk<suspend (map: JsonObject) -> String>().apply {
                coEvery { this@apply.invoke(any<JsonObject>()) } returns Json.encodeToString(
                    listOf(
                        NewsEvent(1, "News event about AI"),
                        NewsEvent(2, "News event about Aigentic"),
                    )
                )
            }

            val newsEventTool = object : Tool {
                override val name = ToolName("getNewsEvents")
                override val description = null
                override val parameters = listOf(
                    Primitive("count", "number of events", true, Integer)
                )
                override val handler: suspend (map: JsonObject) -> String = mockHandler
            }

            val expectedArguments = buildJsonObject { put("count", 10) }

            val modelMock = mockk<Model>().apply {
                coEvery { sendRequest(any(), any()) } returnsMany listOf(
                    ToolCall(ToolCallId("1"), newsEventTool.name.value, expectedArguments.encode()),
                    ToolCall(ToolCallId("2"), finishOrStuckTool.name.value, buildJsonObject {
                        put("finishReason", "FinishedAllTasks")
                        put("description", "There are 2 news events about Aigentic and AI")
                    }.encode())
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
    }
})

@Serializable
data class NewsEvent(val id: Int, val title: String)
