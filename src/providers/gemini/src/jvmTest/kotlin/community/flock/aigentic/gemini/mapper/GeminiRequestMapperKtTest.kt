package community.flock.aigentic.gemini.mapper

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.gemini.client.model.BlobContent
import community.flock.aigentic.gemini.client.model.Part
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json

class GeminiRequestMapperKtTest :
    DescribeSpec({

        describe("Gemini Request Mapper") {

            it("Should not format when raw base64 content is provided") {
                val base64Content = "iVBORw0KGgoAAA=="
                val mimeType = MimeType.PNG
                val base64Message = Message.Base64(Sender.Model, base64Content, mimeType, MessageCategory.EXECUTION)

                createGenerateContentRequest(listOf(base64Message), emptyList(), GenerationSettings.DEFAULT, null)
                    .contents[0]
                    .parts[0]
                    .shouldBeInstanceOf<Part.Blob>()
                    .run {
                        this.inlineData shouldBe BlobContent(mimeType = mimeType.value, data = base64Content)
                    }
            }

            it("should format when base64 data url is provided") {
                val base64Content = "data:image/png;base64,iVBORw0KGgoAAA=="
                val mimeType = MimeType.PNG
                val base64Message = Message.Base64(Sender.Model, base64Content, mimeType, MessageCategory.EXECUTION)

                createGenerateContentRequest(listOf(base64Message), emptyList(), GenerationSettings.DEFAULT, null)
                    .contents[0]
                    .parts[0]
                    .shouldBeInstanceOf<Part.Blob>()
                    .run {
                        this.inlineData shouldBe BlobContent(mimeType = mimeType.value, data = "iVBORw0KGgoAAA==")
                    }
            }

            it("should set max output tokens when configured") {
                val generationSettings = GenerationSettings.DEFAULT.copy(maxOutputTokens = 65536)

                createGenerateContentRequest(emptyList(), emptyList(), generationSettings, null)
                    .generationConfig
                    .maxOutputTokens shouldBe 65536
            }

            it("should omit max output tokens when not configured") {
                createGenerateContentRequest(emptyList(), emptyList(), GenerationSettings.DEFAULT, null)
                    .generationConfig
                    .maxOutputTokens shouldBe null
            }

            it("should include max_output_tokens in the serialized request body when configured") {
                val generationSettings = GenerationSettings.DEFAULT.copy(maxOutputTokens = 65536)
                val request = createGenerateContentRequest(emptyList(), emptyList(), generationSettings, null)

                Json { ignoreUnknownKeys = true }.encodeToString(request) shouldContain "\"max_output_tokens\":65536"
            }

            it("should not serialize a max_output_tokens key when not configured") {
                val request = createGenerateContentRequest(emptyList(), emptyList(), GenerationSettings.DEFAULT, null)

                Json { ignoreUnknownKeys = true }.encodeToString(request) shouldNotContain "max_output_tokens"
            }
        }
    })
