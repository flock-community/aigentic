package community.flock.aigentic.vertexai.request

import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.ThinkingConfig
import community.flock.aigentic.core.model.ThinkingLevel
import community.flock.aigentic.vertexai.VertexAIModelIdentifier
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.util.Optional

class RequestMapperTest :
    DescribeSpec({

        describe("VertexAI Request Mapper") {

            it("should set max output tokens when configured") {
                val generationSettings = GenerationSettings.DEFAULT.copy(maxOutputTokens = 65536)

                createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini2_5Flash)
                    .maxOutputTokens() shouldBe Optional.of(65536)
            }

            it("should omit max output tokens when not configured") {
                createGenerateConfig(emptyList(), emptyList(), GenerationSettings.DEFAULT, null, VertexAIModelIdentifier.Gemini2_5Flash)
                    .maxOutputTokens() shouldBe Optional.empty()
            }

            it("should map thinkingLevel onto the thinking config") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_5Flash)
                    .thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "LOW"
            }

            it("should serialize thinkingLevel in uppercase proto-enum casing on the wire") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                val json =
                    createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_5Flash)
                        .toJson()

                json shouldContain "\"thinkingLevel\":\"LOW\""
                json shouldNotContain "\"thinkingLevel\":\"low\""
            }

            it("should map thinkingBudget onto the thinking config") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini2_5Flash)
                    .thinkingConfig()
                    .get()
                    .thinkingBudget() shouldBe Optional.of(1024)
            }

            it("should throw when thinkingBudget is configured on a 3.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                shouldThrow<Exception> {
                    createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_5Flash)
                }
            }

            it("should throw when thinkingLevel is configured on a 2.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                shouldThrow<Exception> {
                    createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini2_5Flash)
                }
            }

            it("should throw when thinkingLevel MINIMAL is configured on gemini-3.1-pro-preview") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL),
                    )

                shouldThrow<Exception> {
                    createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_1ProPreview)
                }
            }

            it("should allow thinkingBudget on a Custom gemini-2.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("gemini-2.5-x"),
                ).thinkingConfig()
                    .get()
                    .thinkingBudget() shouldBe Optional.of(1024)
            }

            it("should throw when thinkingBudget is configured on a Custom gemini-3.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                shouldThrow<Exception> {
                    createGenerateConfig(
                        emptyList(),
                        emptyList(),
                        generationSettings,
                        null,
                        VertexAIModelIdentifier.Custom("gemini-3.9-flash"),
                    )
                }
            }

            it("should allow thinkingBudget on a Custom identifier that is neither gemini-2.x nor gemini-3.x") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("gemini-1.5-pro"),
                ).thinkingConfig()
                    .get()
                    .thinkingBudget() shouldBe Optional.of(1024)
            }

            it("should allow thinkingLevel on a Custom identifier that is neither gemini-2.x nor gemini-3.x") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("my-proxy"),
                ).thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "LOW"
            }
        }
    })
