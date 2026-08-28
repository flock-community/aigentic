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
                }
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
        }
    })
