package community.flock.aigentic.gemini.client

import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.gemini.client.config.GeminiApiConfig
import community.flock.aigentic.gemini.client.model.GenerateContentRequest
import community.flock.aigentic.gemini.client.model.GenerationConfig
import community.flock.aigentic.gemini.client.ratelimit.RateLimiter
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf

class GeminiClientTest :
    DescribeSpec({

        val okResponse = """{"candidates":[{"content":{"role":"model","parts":[{"text":"ok"}]},"finishReason":"STOP"}],"usageMetadata":{}}"""
        val noRateLimit =
            object : RateLimiter {
                override suspend fun consume() {}
            }
        val request = GenerateContentRequest(contents = emptyList(), generationConfig = GenerationConfig())
        val config = GeminiApiConfig(apiKey = Authentication.APIKey("secret-key"))

        describe("GeminiClient") {

            it("should send the api key as a header, not in the url") {
                val engine =
                    MockEngine { requestData ->
                        requestData.url.toString() shouldNotContain "secret-key"
                        requestData.headers["x-goog-api-key"] shouldBe "secret-key"
                        respond(okResponse, headers = headersOf(HttpHeaders.ContentType, "application/json"))
                    }

                GeminiClient(config, noRateLimit, engine = engine).generateContent(request, GeminiModelIdentifier.Gemini2_5Flash)

                engine.requestHistory.size shouldBe 1
            }

            it("should retry when connecting times out") {
                var attempts = 0
                val engine =
                    MockEngine {
                        attempts++
                        if (attempts == 1) throw ConnectTimeoutException("connect timeout")
                        respond(okResponse, headers = headersOf(HttpHeaders.ContentType, "application/json"))
                    }

                GeminiClient(config, noRateLimit, engine = engine).generateContent(request, GeminiModelIdentifier.Gemini2_5Flash)

                attempts shouldBe 2
            }
        }
    })
