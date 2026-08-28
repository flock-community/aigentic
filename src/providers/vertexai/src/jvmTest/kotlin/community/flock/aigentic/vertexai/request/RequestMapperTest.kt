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
                }.message shouldContain "only supported on Gemini 2.x models"
            }

            it("should throw when thinkingLevel is configured on a 2.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                shouldThrow<Exception> {
                    createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini2_5Flash)
                }.message shouldContain "not supported on Gemini 2.x models"
            }

            it("should map thinkingLevel MINIMAL to LOW on gemini-3.1-pro-preview") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL),
                    )

                createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_1ProPreview)
                    .thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "LOW"
            }

            it("should map thinkingLevel MINIMAL to LOW on gemini-3.7-flash") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL),
                    )

                createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_7Flash)
                    .thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "LOW"
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
                }.message shouldContain "only supported on Gemini 2.x models"
            }

            it("should allow thinkingBudget on a Custom identifier with major version 1") {
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

            it("should allow thinkingLevel on a Custom identifier that has no version match") {
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

            it("should allow thinkingBudget on a Custom identifier with the models/ prefix") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("models/gemini-2.5-flash"),
                ).thinkingConfig()
                    .get()
                    .thinkingBudget() shouldBe Optional.of(1024)
            }

            it("should throw when thinkingLevel is configured on a Custom identifier with the models/ prefix") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                shouldThrow<Exception> {
                    createGenerateConfig(
                        emptyList(),
                        emptyList(),
                        generationSettings,
                        null,
                        VertexAIModelIdentifier.Custom("models/gemini-2.5-flash"),
                    )
                }.message shouldContain "not supported on Gemini 2.x models"
            }

            it("should allow thinkingLevel on a Custom identifier with major version 4") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("gemini-4.0-pro"),
                ).thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "MINIMAL"
            }

            it("should throw when thinkingBudget is configured on a Custom identifier with major version 4") {
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
                        VertexAIModelIdentifier.Custom("gemini-4.0-pro"),
                    )
                }.message shouldContain "only supported on Gemini 2.x models"
            }

            it("should allow thinkingBudget on a Custom gemini-exp-1206 identifier") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("gemini-exp-1206"),
                ).thinkingConfig()
                    .get()
                    .thinkingBudget() shouldBe Optional.of(1024)
            }

            it("should allow thinkingLevel on a Custom gemini-exp-1206 identifier") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("gemini-exp-1206"),
                ).thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "LOW"
            }
        }
    })
