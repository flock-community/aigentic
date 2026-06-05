package community.flock.aigentic.platform.client

import community.flock.aigentic.core.agent.Expected
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.addToEvaluationSet
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.model.Usage
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.EvaluationSubmitResult
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.gateway.wirespec.model.RunDto
import community.flock.aigentic.gateway.wirespec.model.RunEvaluationDto
import community.flock.aigentic.platform.AigenticPlatform
import community.flock.aigentic.platform.mapper.toDto
import community.flock.aigentic.platform.util.createAgent
import community.flock.aigentic.platform.util.createAgentRun
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.serializer

@AigenticParameter
private data class InvoiceFields(
    val invoiceNumber: String,
    val total: String,
)

private val lenientJson = Json { ignoreUnknownKeys = true }

private val finishedInvoice = InvoiceFields("INV-001", "1250.00")

private fun finishedTaskToolCall(): ToolCall =
    ToolCall(
        ToolCallId("1"),
        FINISHED_TASK_TOOL_NAME,
        Json.encodeToString(
            JsonObject.serializer(),
            buildJsonObject {
                put("description", JsonPrimitive("Finished the task"))
                put(
                    "InvoiceFields",
                    buildJsonObject {
                        put("invoiceNumber", JsonPrimitive(finishedInvoice.invoiceNumber))
                        put("total", JsonPrimitive(finishedInvoice.total))
                    },
                )
            },
        ),
    )

private val finishedInvoiceModel: Model =
    object : Model {
        override val modelIdentifier: ModelIdentifier =
            object : ModelIdentifier {
                override val stringValue = "test-model"
            }
        override val generationSettings = GenerationSettings.DEFAULT

        override suspend fun sendRequest(
            messages: List<Message>,
            tools: List<ToolDescription>,
            structuredOutputParameter: Parameter?,
        ): ModelResponse =
            ModelResponse(
                message = Message.ToolCalls(listOf(finishedTaskToolCall())),
                usage = Usage(inputTokenCount = 1, outputTokenCount = 1, thinkingOutputTokenCount = 0),
            )
    }

private fun structuredAgent(platform: Platform) =
    agent<Unit, InvoiceFields> {
        platform(platform)
        model(finishedInvoiceModel)
        task("Extract the invoice fields") {}
    }

private fun mockPlatform(engine: MockEngine): Platform {
    val auth = Authentication.BasicAuth("user", "pass")
    val url = PlatformApiUrl("")
    return AigenticPlatform(
        authentication = auth,
        apiUrl = url,
        client =
            AigenticPlatformClient(
                basicAuth = auth,
                apiUrl = url,
                endpoints = AigenticPlatformEndpoints(auth, url, engine),
            ),
    )
}

private suspend fun HttpRequestData.bodyText(): String = body.toByteArray().decodeToString()

class EvaluationSubmissionTest :
    DescribeSpec({

        describe("RequestMapper evaluation field") {

            it("builds RunEvaluationDto with evaluationSet and serialized output when expected is present") {
                val agent = createAgent()
                val run = createAgentRun()

                val dto =
                    run.toDto(
                        agent,
                        serializer<String>(),
                        Expected(evaluationSet = "golden-set", output = "the-expected-output"),
                    )

                val evaluation = dto.evaluation.shouldNotBeNull()
                evaluation.evaluationSet shouldBe "golden-set"
                evaluation.expectedResponse shouldBe Json.encodeToString(serializer<String>(), "the-expected-output")
            }

            it("leaves evaluation null when expected is absent") {
                val agent = createAgent()
                val run = createAgentRun()

                val dto = run.toDto(agent, serializer<String>(), null)

                dto.evaluation shouldBe null
            }
        }

        describe("start with expected") {

            it("puts the serialized expected output and evaluationSet in the POST body") {
                var capturedBody: String? = null
                val engine =
                    MockEngine { request ->
                        when (request.method to request.url.encodedPath) {
                            (HttpMethod.Post to "/gateway/runs") -> {
                                capturedBody = request.bodyText()
                                respond(
                                    content = ByteReadChannel("""{"runId":"run-1"}"""),
                                    status = HttpStatusCode.Created,
                                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                                )
                            }

                            else -> {
                                error("Unexpected endpoint called! ${request.url.encodedPath}")
                            }
                        }
                    }

                val agent = structuredAgent(mockPlatform(engine))

                agent.start(
                    expected = Expected(evaluationSet = "invoice-golden-set", output = finishedInvoice),
                )

                val body = capturedBody.shouldNotBeNull()
                val runDto = lenientJson.decodeFromString(RunDto.serializer(), body)
                val evaluation = runDto.evaluation.shouldNotBeNull()
                evaluation.evaluationSet shouldBe "invoice-golden-set"
                evaluation.expectedResponse shouldBe Json.encodeToString(serializer<InvoiceFields>(), finishedInvoice)
            }

            it("populates run.platformRunId from the 201 RunCreatedDto body") {
                val engine =
                    MockEngine { request ->
                        when (request.method to request.url.encodedPath) {
                            (HttpMethod.Post to "/gateway/runs") -> {
                                respond(
                                    content = ByteReadChannel("""{"runId":"run-42"}"""),
                                    status = HttpStatusCode.Created,
                                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                                )
                            }

                            else -> {
                                error("Unexpected endpoint called! ${request.url.encodedPath}")
                            }
                        }
                    }

                val agent = structuredAgent(mockPlatform(engine))

                val run = agent.start()

                run.platformRunId shouldBe RunId("run-42")
            }

            it("leaves run.platformRunId null when the 201 body is empty (old gateways)") {
                val engine =
                    MockEngine { request ->
                        when (request.method to request.url.encodedPath) {
                            (HttpMethod.Post to "/gateway/runs") -> {
                                respond(content = ByteReadChannel.Empty, status = HttpStatusCode.Created)
                            }

                            else -> {
                                error("Unexpected endpoint called! ${request.url.encodedPath}")
                            }
                        }
                    }

                val agent = structuredAgent(mockPlatform(engine))

                val run = agent.start()

                run.platformRunId shouldBe null
            }
        }

        describe("addToEvaluationSet") {

            it("POSTs the serialized expected output to /gateway/runs/{runId}/evaluation") {
                var capturedPath: String? = null
                var capturedBody: String? = null
                val engine =
                    MockEngine { request ->
                        capturedPath = request.url.encodedPath
                        capturedBody = request.bodyText()
                        respond(content = ByteReadChannel.Empty, status = HttpStatusCode.OK)
                    }

                val agent = structuredAgent(mockPlatform(engine))

                val result =
                    agent.addToEvaluationSet(
                        runId = RunId("run-99"),
                        evaluationSet = "golden",
                        expected = InvoiceFields("INV-009", "9.99"),
                    )

                result shouldBe EvaluationSubmitResult.Success
                capturedPath shouldBe "/gateway/runs/run-99/evaluation"
                val body = capturedBody.shouldNotBeNull()
                val evaluationDto = lenientJson.decodeFromString(RunEvaluationDto.serializer(), body)
                evaluationDto.evaluationSet shouldBe "golden"
                evaluationDto.expectedResponse shouldBe
                    Json.encodeToString(serializer<InvoiceFields>(), InvoiceFields("INV-009", "9.99"))
            }
        }
    })
