package community.flock.aigentic.gemini.model

import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.ThinkingConfig
import community.flock.aigentic.core.model.ThinkingLevel
import community.flock.aigentic.gemini.client.GeminiClient
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.mockk

class GeminiModelTest :
    DescribeSpec({

        describe("GeminiModelIdentifier") {

            it("should expose the correct wire identifiers") {
                GeminiModelIdentifier.Gemini3_7Flash.stringValue shouldBe "gemini-3.7-flash"
                GeminiModelIdentifier.Gemini3_6Flash.stringValue shouldBe "gemini-3.6-flash"
                GeminiModelIdentifier.Gemini3_5FlashLite.stringValue shouldBe "gemini-3.5-flash-lite"
            }
        }

        describe("GeminiModel") {

            it("should throw when built with an invalid thinking configuration") {
                shouldThrow<Exception> {
                    GeminiModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = GeminiModelIdentifier.Gemini2_5Flash,
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                        geminiClient = mockk<GeminiClient>(relaxed = true),
                    )
                }.message shouldContain "not supported on Gemini 2.x models"
            }

            it("should build successfully with a valid thinking configuration") {
                shouldNotThrowAny {
                    GeminiModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = GeminiModelIdentifier.Gemini2_5Flash,
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                            ),
                        geminiClient = mockk<GeminiClient>(relaxed = true),
                    )
                }
            }

            it("should allow thinkingBudget on a Custom identifier with the models/ prefix") {
                shouldNotThrowAny {
                    validateGeminiThinkingConfig(
                        modelIdentifier = GeminiModelIdentifier.Custom("models/gemini-2.5-flash"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                            ),
                    )
                }
            }

            it("should throw when thinkingLevel is configured on a Custom identifier with the models/ prefix") {
                shouldThrow<Exception> {
                    validateGeminiThinkingConfig(
                        modelIdentifier = GeminiModelIdentifier.Custom("models/gemini-2.5-flash"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                    )
                }.message shouldContain "not supported on Gemini 2.x models"
            }

            it("should allow thinkingLevel on a Custom identifier with major version 4") {
                shouldNotThrowAny {
                    validateGeminiThinkingConfig(
                        modelIdentifier = GeminiModelIdentifier.Custom("gemini-4.0-pro"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                    )
                }
            }

            it("should allow thinkingLevel on a Custom identifier with a resource-path prefix and major version 4") {
                shouldNotThrowAny {
                    validateGeminiThinkingConfig(
                        modelIdentifier = GeminiModelIdentifier.Custom("publishers/google/models/gemini-4.0-pro"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                    )
                }
            }

            it("should throw when thinkingBudget is configured on a Custom identifier with major version 4") {
                shouldThrow<Exception> {
                    validateGeminiThinkingConfig(
                        modelIdentifier = GeminiModelIdentifier.Custom("gemini-4.0-pro"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                            ),
                    )
                }.message shouldContain "only supported on Gemini 2.x models"
            }

            it("should allow thinkingBudget and thinkingLevel on a Custom gemini-exp-1206 identifier") {
                shouldNotThrowAny {
                    validateGeminiThinkingConfig(
                        modelIdentifier = GeminiModelIdentifier.Custom("gemini-exp-1206"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                            ),
                    )
                    validateGeminiThinkingConfig(
                        modelIdentifier = GeminiModelIdentifier.Custom("gemini-exp-1206"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                    )
                }
            }

            it("should allow thinkingBudget on a Custom identifier with major version 1") {
                shouldNotThrowAny {
                    validateGeminiThinkingConfig(
                        modelIdentifier = GeminiModelIdentifier.Custom("gemini-1.5-pro"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                            ),
                    )
                }
            }
        }
    })
