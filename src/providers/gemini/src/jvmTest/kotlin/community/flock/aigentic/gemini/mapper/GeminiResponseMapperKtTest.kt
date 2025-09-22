package community.flock.aigentic.gemini.mapper

import community.flock.aigentic.gemini.client.model.FinishReason
import community.flock.aigentic.gemini.client.model.GenerateContentResponse
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

class GeminiResponseMapperKtTest : DescribeSpec({

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
    }
})
