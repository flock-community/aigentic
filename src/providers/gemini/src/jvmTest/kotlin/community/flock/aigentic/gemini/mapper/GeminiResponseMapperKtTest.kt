package community.flock.aigentic.gemini.mapper

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.gemini.client.geminiJson
import community.flock.aigentic.gemini.client.model.FinishReason
import community.flock.aigentic.gemini.client.model.GenerateContentResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json

class GeminiResponseMapperKtTest :
    DescribeSpec({

        describe("Gemini Response Mapper") {
            it("should deserialize MALFORMED_FUNCTION_CALL and map to ModelResponse") {
                val json =
                    """
                    {
                      "candidates": [
                        {
                          "content": {
                            "role": "model",
                            "parts": [
                              { "text": "The function call was malformed." }
                            ]
                          },
                          "finishReason": "MALFORMED_FUNCTION_CALL"
                        }
                      ],
                      "usageMetadata": {
                        "promptTokenCount": 10,
                        "candidatesTokenCount": 5,
                        "totalTokenCount": 15
                      }
                    }
                    """.trimIndent()

                val parsed = Json { ignoreUnknownKeys = true }.decodeFromString<GenerateContentResponse>(json)

                parsed.candidates?.first()?.finishReason shouldBe FinishReason.MALFORMED_FUNCTION_CALL

                // Ensure mapping does not throw and returns a ModelResponse
                val modelResponse = parsed.toModelResponse(false)
                modelResponse.usage.inputTokenCount shouldBe 10
                modelResponse.usage.outputTokenCount shouldBe 5
            }

            it("should handle candidate without content gracefully") {
                val json =
                    """
                    {
                      "candidates": [
                        {
                          "finishReason": "SAFETY"
                        }
                      ],
                      "usageMetadata": {
                        "promptTokenCount": 10,
                        "candidatesTokenCount": 0,
                        "totalTokenCount": 10
                      }
                    }
                    """.trimIndent()

                val parsed = Json { ignoreUnknownKeys = true }.decodeFromString<GenerateContentResponse>(json)

                parsed.candidates?.first()?.content shouldBe null
                parsed.candidates?.first()?.finishReason shouldBe FinishReason.SAFETY
            }

            it("should fail with a truncation message when the max output token limit is reached") {
                val json =
                    """
                    {
                      "candidates": [
                        {
                          "content": {
                            "role": "model",
                            "parts": [
                              { "text": "{\"answer\": \"a partial respo" }
                            ]
                          },
                          "finishReason": "MAX_TOKENS"
                        }
                      ],
                      "usageMetadata": {
                        "promptTokenCount": 10,
                        "candidatesTokenCount": 8192,
                        "totalTokenCount": 8202
                      }
                    }
                    """.trimIndent()

                val parsed = geminiJson.decodeFromString<GenerateContentResponse>(json)

                shouldThrow<Exception> {
                    parsed.toModelResponse(true)
                }.message shouldContain "truncated"
            }

            it("should map a structured output response that finished normally") {
                val json =
                    """
                    {
                      "candidates": [
                        {
                          "content": {
                            "role": "model",
                            "parts": [
                              { "text": "{\"answer\": \"complete\"}" }
                            ]
                          },
                          "finishReason": "STOP"
                        }
                      ],
                      "usageMetadata": {
                        "promptTokenCount": 10,
                        "candidatesTokenCount": 5,
                        "totalTokenCount": 15
                      }
                    }
                    """.trimIndent()

                val parsed = geminiJson.decodeFromString<GenerateContentResponse>(json)

                parsed
                    .toModelResponse(true)
                    .message
                    .shouldBeInstanceOf<Message.StructuredOutput>()
                    .response shouldBe """{"answer": "complete"}"""
            }
        }
    })
