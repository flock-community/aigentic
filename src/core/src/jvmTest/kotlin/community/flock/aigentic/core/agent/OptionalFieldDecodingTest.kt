package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.test.util.encode
import community.flock.aigentic.core.agent.test.util.toModelResponse
import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import community.flock.aigentic.core.agent.tool.Outcome.Finished
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.model.Usage
import community.flock.aigentic.core.tool.createTool
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

enum class LengthUnit {
    MM,
    CM,
}

@AigenticParameter
data class Dimensions(
    val value: Double,
    val unit: LengthUnit?,
)

@AigenticParameter
data class Extraction(
    val title: String,
    val pages: Int?,
    val unit: LengthUnit?,
    val dimensions: Dimensions?,
    val tags: List<String>?,
)

class OptionalFieldDecodingTest :
    DescribeSpec({

        val withoutValues = Extraction(title = "Report", pages = null, unit = null, dimensions = null, tags = null)

        val responses =
            mapOf(
                "null" to
                    buildJsonObject {
                        put("title", "Report")
                        put("pages", JsonNull)
                        put("unit", JsonNull)
                        put("dimensions", JsonNull)
                        put("tags", JsonNull)
                    },
                "left out" to
                    buildJsonObject {
                        put("title", "Report")
                    },
            )

        fun modelReturning(response: ModelResponse): Model =
            mockk<Model>().apply {
                coEvery { sendRequest(any(), any(), any()) } returns response
            }

        fun finishedTaskModel(response: JsonObject): Model =
            modelReturning(
                ToolCall(
                    ToolCallId("1"),
                    FINISHED_TASK_TOOL_NAME,
                    buildJsonObject {
                        put("description", "Finished the task")
                        put("Extraction", response)
                    }.encode(),
                ).toModelResponse(),
            )

        responses.forEach { (case, response) ->

            it("should decode optional fields that are $case as null in the finished task response") {
                val agent =
                    agent<Unit, Extraction> {
                        model(finishedTaskModel(response))
                        task("Extract the report") {}
                    }

                agent
                    .start()
                    .outcome
                    .shouldBeTypeOf<Finished<Extraction>>()
                    .response shouldBe withoutValues
            }

            it("should decode optional fields that are $case as null in a structured output response") {
                val agent =
                    agent<Unit, Extraction> {
                        model(modelReturning(ModelResponse(Message.StructuredOutput(response.encode()), Usage.EMPTY)))
                        task("Extract the report") {}
                    }

                agent
                    .start()
                    .outcome
                    .shouldBeTypeOf<Finished<Extraction>>()
                    .response shouldBe withoutValues
            }

            it("should decode optional fields that are $case as null in tool arguments") {
                var received: Extraction? = null
                val tool =
                    createTool<Extraction, String>("extract") {
                        received = it
                        "Extracted"
                    }

                tool.handler(response)

                received shouldBe withoutValues
            }
        }

        it("should decode optional fields that have a value") {
            val response =
                buildJsonObject {
                    put("title", "Report")
                    put("pages", 3)
                    put("unit", "MM")
                    put(
                        "dimensions",
                        buildJsonObject {
                            put("value", 1.5)
                            put("unit", JsonNull)
                        },
                    )
                    putJsonArray("tags") { add("draft") }
                }
            val agent =
                agent<Unit, Extraction> {
                    model(finishedTaskModel(response))
                    task("Extract the report") {}
                }

            agent
                .start()
                .outcome
                .shouldBeTypeOf<Finished<Extraction>>()
                .response shouldBe
                Extraction(
                    title = "Report",
                    pages = 3,
                    unit = LengthUnit.MM,
                    dimensions = Dimensions(value = 1.5, unit = null),
                    tags = listOf("draft"),
                )
        }
    })
