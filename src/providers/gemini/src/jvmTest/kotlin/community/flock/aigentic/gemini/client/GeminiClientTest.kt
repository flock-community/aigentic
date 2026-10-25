package community.flock.aigentic.gemini.client

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.gemini.client.config.GeminiApiConfig
import community.flock.aigentic.gemini.client.model.FinishReason
import community.flock.aigentic.gemini.client.model.GenerateContentRequest
import community.flock.aigentic.gemini.client.model.GenerationConfig
import community.flock.aigentic.gemini.client.model.Part
import community.flock.aigentic.gemini.client.ratelimit.RateLimiter
import community.flock.aigentic.gemini.mapper.toModelResponse
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.writeStringUtf8
import io.ktor.utils.io.writer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.io.EOFException
import kotlin.coroutines.cancellation.CancellationException

class GeminiClientTest :
    DescribeSpec({

        fun sse(vararg chunks: String) = chunks.joinToString("") { "data: $it\r\n\r\n" }

        val sseHeaders = headersOf(HttpHeaders.ContentType, "text/event-stream")
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
        val okResponse = sse("""{"candidates":[{"content":{"role":"model","parts":[{"text":"ok"}]},"finishReason":"STOP"}],"usageMetadata":{}}""")
        val noRateLimit =
            object : RateLimiter {
                override suspend fun consume() {}
            }
        val request = GenerateContentRequest(contents = emptyList(), generationConfig = GenerationConfig())
        val config = GeminiApiConfig(apiKey = Authentication.APIKey("secret-key"))

        suspend fun generate(
            engine: MockEngine,
            clientConfig: GeminiApiConfig = config,
        ) = GeminiClient(clientConfig, noRateLimit, engine = engine).generateContent(request, GeminiModelIdentifier.Gemini3_8Flash)

        suspend fun generateFromStream(vararg chunks: String) = generate(MockEngine { respond(sse(*chunks), headers = sseHeaders) })

        describe("GeminiClient") {

            it("should send the api key as a header, not in the url") {
                val engine =
                    MockEngine { requestData ->
                        requestData.url.toString() shouldNotContain "secret-key"
                        requestData.headers["x-goog-api-key"] shouldBe "secret-key"
                        respond(okResponse, headers = sseHeaders)
                    }

                generate(engine)

                engine.requestHistory.size shouldBe 1
            }

            it("should stream the response with server-sent events") {
                val engine = MockEngine { respond(okResponse, headers = sseHeaders) }

                generate(engine)

                engine.requestHistory
                    .single()
                    .url
                    .toString() shouldEndWith "/gemini-3.8-flash:streamGenerateContent?alt=sse"
            }

            it("should retry when connecting times out") {
                var attempts = 0
                val engine =
                    MockEngine {
                        attempts++
                        if (attempts == 1) throw ConnectTimeoutException("connect timeout")
                        respond(okResponse, headers = sseHeaders)
                    }

                generate(engine)

                attempts shouldBe 2
            }

            it("should merge the chunks into one response with the usage of the last chunk") {
                val response =
                    generateFromStream(
                        """{"candidates":[{"content":{"role":"model","parts":[{"text":"{\"answer\":"}]}}],"usageMetadata":{"promptTokenCount":10}}""",
                        """{"candidates":[{"content":{"role":"model","parts":[{"text":" \"complete\"}"}]}}],"usageMetadata":{"promptTokenCount":10}}""",
                        """{"candidates":[{"content":{"role":"model","parts":[{"text":""}]},"finishReason":"STOP"}],"usageMetadata":{"promptTokenCount":10,"candidatesTokenCount":7,"thoughtsTokenCount":3}}""",
                    )

                response.candidates!!.single().finishReason shouldBe FinishReason.STOP
                response.toModelResponse(isStructuredOutput = true).apply {
                    message.shouldBeInstanceOf<Message.StructuredOutput>().response shouldBe """{"answer": "complete"}"""
                    usage.outputTokenCount shouldBe 7
                    usage.thinkingOutputTokenCount shouldBe 3
                }
            }

            it("should keep the thought signature of a streamed function call") {
                val response =
                    generateFromStream(
                        """{"candidates":[{"content":{"role":"model","parts":[{"functionCall":{"name":"lookup","args":{"id":1}},"thoughtSignature":"sig-abc"}]}}]}""",
                        """{"candidates":[{"content":{"role":"model","parts":[{"text":""}]},"finishReason":"STOP"}],"usageMetadata":{}}""",
                    )

                response.candidates!!
                    .single()
                    .content!!
                    .parts
                    .filterIsInstance<Part.FunctionCall>()
                    .single()
                    .thoughtSignature shouldBe "sig-abc"
            }

            it("should fail when the stream contains an error") {
                shouldThrow<Exception> {
                    generateFromStream(
                        """{"candidates":[{"content":{"role":"model","parts":[{"text":"{"}]}}]}""",
                        """{"error":{"code":500,"message":"Internal error encountered.","status":"INTERNAL"}}""",
                    )
                }.message shouldBe
                    "Received error from Gemini while streaming the response, error code: INTERNAL, message: Internal error encountered."
            }

            it("should fail when the stream ends before the response is complete") {
                shouldThrow<Exception> {
                    generateFromStream("""{"candidates":[{"content":{"role":"model","parts":[{"text":"{\"answer\":"}]}}]}""")
                }.message shouldBe "Gemini stream ended before the response was complete"
            }

            it("should return a blocked prompt without a finish reason so it can be reported") {
                val response = generateFromStream("""{"promptFeedback":{"blockReason":"SAFETY"},"usageMetadata":{}}""")

                response.promptFeedback!!.blockReason.toString() shouldBe "SAFETY"
                response.candidates shouldBe null
            }

            it("should map an error response") {
                val engine =
                    MockEngine {
                        respond(
                            """{"error":{"code":400,"message":"Invalid thinking level.","status":"INVALID_ARGUMENT"}}""",
                            status = HttpStatusCode.BadRequest,
                            headers = jsonHeaders,
                        )
                    }

                shouldThrow<Exception> { generate(engine) }.message shouldBe
                    "Received error response from Gemini, http status: 400 Bad Request, error code: INVALID_ARGUMENT, message: Invalid thinking level."
            }

            it("should map an error response the streaming endpoint sends as text/event-stream") {
                val engine =
                    MockEngine {
                        respond(
                            """{"error":{"code":402,"message":"Your prepayment credits are depleted.","status":"RESOURCE_EXHAUSTED"}}""",
                            status = HttpStatusCode.PaymentRequired,
                            headers = sseHeaders,
                        )
                    }

                shouldThrow<Exception> { generate(engine) }.message shouldBe
                    "Received error response from Gemini, http status: 402 Payment Required, error code: RESOURCE_EXHAUSTED, " +
                    "message: Your prepayment credits are depleted."
            }

            it("should show an error body that is not Gemini's error JSON") {
                val engine = MockEngine { respond("model not found", status = HttpStatusCode.NotFound) }

                shouldThrow<Exception> { generate(engine) }.message shouldBe
                    "Received error response from Gemini, http status: 404 Not Found, body: model not found"
            }

            it("should wrap transport failures") {
                val engine = MockEngine { throw EOFException("Failed to parse HTTP response: the server prematurely closed the connection") }

                shouldThrow<Exception> { generate(engine) }.message shouldBe
                    "Gemini request failed: Failed to parse HTTP response: the server prematurely closed the connection"
            }

            it("should wrap a request timeout while streaming") {
                val engine =
                    MockEngine { requestData ->
                        respond(
                            CoroutineScope(requestData.executionContext)
                                .writer {
                                    channel.writeStringUtf8(
                                        sse("""{"candidates":[{"content":{"role":"model","parts":[{"text":"{"}]}}]}"""),
                                    )
                                    channel.flush()
                                    awaitCancellation()
                                }.channel,
                            headers = sseHeaders,
                        )
                    }

                val exception =
                    shouldThrow<Exception> {
                        generate(engine, config.copy(numberOfRetriesOnNetworkErrors = 0, requestTimeoutMillis = 200))
                    }

                exception.message shouldContain "Gemini request failed: Request timeout has expired"
                exception.shouldNotBeInstanceOf<CancellationException>()
            }
        }
    })
