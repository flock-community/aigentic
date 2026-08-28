package community.flock.aigentic.gemini.mapper

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.ThinkingConfig
import community.flock.aigentic.core.model.ThinkingLevel
import community.flock.aigentic.gemini.client.geminiJson
import community.flock.aigentic.gemini.client.model.BlobContent
import community.flock.aigentic.gemini.client.model.Part
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf

class GeminiRequestMapperKtTest :
    DescribeSpec({

        describe("Gemini Request Mapper") {

            it("Should not format when raw base64 content is provided") {
                val base64Content = "iVBORw0KGgoAAA=="
                val mimeType = MimeType.PNG
                val base64Message = Message.Base64(Sender.Model, base64Content, mimeType, MessageCategory.EXECUTION)

                createGenerateContentRequest(listOf(base64Message), emptyList(), GenerationSettings.DEFAULT, null, GeminiModelIdentifier.Gemini2_5Flash)
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

                createGenerateContentRequest(listOf(base64Message), emptyList(), GenerationSettings.DEFAULT, null, GeminiModelIdentifier.Gemini2_5Flash)
                    .contents[0]
                    .parts[0]
                    .shouldBeInstanceOf<Part.Blob>()
                    .run {
                        this.inlineData shouldBe BlobContent(mimeType = mimeType.value, data = "iVBORw0KGgoAAA==")
                    }
            }

            it("should set max output tokens when configured") {
                val generationSettings = GenerationSettings.DEFAULT.copy(maxOutputTokens = 65536)

                createGenerateContentRequest(emptyList(), emptyList(), generationSettings, null, GeminiModelIdentifier.Gemini2_5Flash)
                    .generationConfig
                    .maxOutputTokens shouldBe 65536
            }

            it("should omit max output tokens when not configured") {
                createGenerateContentRequest(emptyList(), emptyList(), GenerationSettings.DEFAULT, null, GeminiModelIdentifier.Gemini2_5Flash)
                    .generationConfig
                    .maxOutputTokens shouldBe null
            }

            it("should include max_output_tokens in the serialized request body when configured") {
                val generationSettings = GenerationSettings.DEFAULT.copy(maxOutputTokens = 65536)
                val request = createGenerateContentRequest(emptyList(), emptyList(), generationSettings, null, GeminiModelIdentifier.Gemini2_5Flash)

                geminiJson.encodeToString(request) shouldContain "\"max_output_tokens\":65536"
            }

            it("should not serialize a max_output_tokens key when not configured") {
                val request = createGenerateContentRequest(emptyList(), emptyList(), GenerationSettings.DEFAULT, null, GeminiModelIdentifier.Gemini2_5Flash)

                geminiJson.encodeToString(request) shouldNotContain "max_output_tokens"
            }

            it("should serialize thinkingLevel and omit thinkingBudget when a level is configured") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )
                val request =
                    createGenerateContentRequest(emptyList(), emptyList(), generationSettings, null, GeminiModelIdentifier.Gemini3_5Flash)

                geminiJson.encodeToString(request) shouldContain "\"thinkingLevel\":\"low\""
                geminiJson.encodeToString(request) shouldNotContain "thinkingBudget"
            }

            it("should serialize thinkingBudget and omit thinkingLevel when a budget is configured") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )
                val request =
                    createGenerateContentRequest(emptyList(), emptyList(), generationSettings, null, GeminiModelIdentifier.Gemini2_5Flash)

                geminiJson.encodeToString(request) shouldContain "\"thinkingBudget\":1024"
                geminiJson.encodeToString(request) shouldNotContain "thinkingLevel"
            }

            it("should allow thinkingBudget on a 2.5 model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateContentRequest(emptyList(), emptyList(), generationSettings, null, GeminiModelIdentifier.Gemini2_5Flash)
                    .generationConfig
                    .thinkingConfig
                    ?.thinkingBudget shouldBe 1024
            }

            it("should throw when thinkingBudget is configured on a 3.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                shouldThrow<Exception> {
                    createGenerateContentRequest(emptyList(), emptyList(), generationSettings, null, GeminiModelIdentifier.Gemini3_5Flash)
                }
            }

            it("should throw when thinkingLevel is configured on a 2.5 model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                shouldThrow<Exception> {
                    createGenerateContentRequest(emptyList(), emptyList(), generationSettings, null, GeminiModelIdentifier.Gemini2_5Flash)
                }
            }

            it("should throw when thinkingLevel MINIMAL is configured on gemini-3.7-flash") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL),
                    )

                shouldThrow<Exception> {
                    createGenerateContentRequest(emptyList(), emptyList(), generationSettings, null, GeminiModelIdentifier.Gemini3_7Flash)
                }
            }

            it("should allow thinkingLevel MINIMAL on gemini-3.6-flash") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL),
                    )

                createGenerateContentRequest(emptyList(), emptyList(), generationSettings, null, GeminiModelIdentifier.Gemini3_6Flash)
                    .generationConfig
                    .thinkingConfig
                    ?.thinkingLevel shouldBe "minimal"
            }

            it("should throw when thinkingLevel is configured on a Custom gemini-2.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                shouldThrow<Exception> {
                    createGenerateContentRequest(
                        emptyList(),
                        emptyList(),
                        generationSettings,
                        null,
                        GeminiModelIdentifier.Custom("gemini-2.5-x"),
                    )
                }
            }

            it("should allow thinkingLevel on a Custom non-gemini-2.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                createGenerateContentRequest(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    GeminiModelIdentifier.Custom("my-model"),
                ).generationConfig
                    .thinkingConfig
                    ?.thinkingLevel shouldBe "low"
            }

            it("should not include a thinkingConfig key when not configured") {
                val request = createGenerateContentRequest(emptyList(), emptyList(), GenerationSettings.DEFAULT, null, GeminiModelIdentifier.Gemini2_5Flash)

                geminiJson.encodeToString(request) shouldNotContain "thinkingConfig"
            }

            it("should not include a thinkingConfig key when a ThinkingConfig with both fields null is configured") {
                val generationSettings = GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig())
                val request = createGenerateContentRequest(emptyList(), emptyList(), generationSettings, null, GeminiModelIdentifier.Gemini2_5Flash)

                geminiJson.encodeToString(request) shouldNotContain "thinkingConfig"
            }

            it("should allow thinkingBudget on a Custom gemini-2.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateContentRequest(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    GeminiModelIdentifier.Custom("gemini-2.5-x"),
                ).generationConfig
                    .thinkingConfig
                    ?.thinkingBudget shouldBe 1024
            }

            it("should throw when thinkingBudget is configured on a Custom gemini-3.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                shouldThrow<Exception> {
                    createGenerateContentRequest(
                        emptyList(),
                        emptyList(),
                        generationSettings,
                        null,
                        GeminiModelIdentifier.Custom("gemini-3.9-flash"),
                    )
                }
            }

            it("should allow thinkingBudget on a Custom identifier that is neither gemini-2.x nor gemini-3.x") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateContentRequest(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    GeminiModelIdentifier.Custom("gemini-1.5-pro"),
                ).generationConfig
                    .thinkingConfig
                    ?.thinkingBudget shouldBe 1024
            }

            it("should allow thinkingLevel on a Custom identifier that is neither gemini-2.x nor gemini-3.x") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                createGenerateContentRequest(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    GeminiModelIdentifier.Custom("my-proxy"),
                ).generationConfig
                    .thinkingConfig
                    ?.thinkingLevel shouldBe "low"
            }
        }
    })
