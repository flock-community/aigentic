package community.flock.aigentic.vertexai

import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ThinkingConfig
import community.flock.aigentic.core.model.ThinkingLevel
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private object OtherModelIdentifier : ModelIdentifier {
    override val stringValue: String = "other-model"
}

class VertexAIModelTest :
    DescribeSpec({

        describe("VertexAIModelIdentifier") {

            it("should expose the correct wire identifiers") {
                VertexAIModelIdentifier.Gemini3_7Flash.stringValue shouldBe "gemini-3.7-flash"
                VertexAIModelIdentifier.Gemini3_6Flash.stringValue shouldBe "gemini-3.6-flash"
                VertexAIModelIdentifier.Gemini3_5FlashLite.stringValue shouldBe "gemini-3.5-flash-lite"
            }
        }

        describe("VertexAIModel") {

            it("should throw when built with an invalid thinking configuration") {
                shouldThrow<Exception> {
                    VertexAIModel(
                        modelIdentifier = VertexAIModelIdentifier.Gemini2_5Flash,
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                        project = Project("project"),
                        location = Location("location"),
                        requestTimeoutMillis = 60_000,
                    )
                }
            }

            it("should build successfully with a valid thinking configuration") {
                shouldNotThrowAny {
                    validateVertexAIThinkingConfig(
                        modelIdentifier = VertexAIModelIdentifier.Gemini2_5Flash,
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                            ),
                    )
                }
            }

            it("should not validate an unrelated ModelIdentifier implementation") {
                shouldNotThrowAny {
                    validateVertexAIThinkingConfig(
                        modelIdentifier = OtherModelIdentifier,
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                    )
                }
            }

            it("should allow thinkingBudget on a Custom identifier with the models/ prefix") {
                shouldNotThrowAny {
                    validateVertexAIThinkingConfig(
                        modelIdentifier = VertexAIModelIdentifier.Custom("models/gemini-2.5-flash"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                            ),
                    )
                }
            }

            it("should throw when thinkingLevel is configured on a Custom identifier with the models/ prefix") {
                shouldThrow<Exception> {
                    validateVertexAIThinkingConfig(
                        modelIdentifier = VertexAIModelIdentifier.Custom("models/gemini-2.5-flash"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                    )
                }
            }

            it("should allow thinkingLevel on a Custom identifier with major version 4") {
                shouldNotThrowAny {
                    validateVertexAIThinkingConfig(
                        modelIdentifier = VertexAIModelIdentifier.Custom("gemini-4.0-pro"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                    )
                }
            }

            it("should throw when thinkingBudget is configured on a Custom identifier with major version 4") {
                shouldThrow<Exception> {
                    validateVertexAIThinkingConfig(
                        modelIdentifier = VertexAIModelIdentifier.Custom("gemini-4.0-pro"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                            ),
                    )
                }
            }

            it("should allow thinkingBudget and thinkingLevel on a Custom gemini-exp-1206 identifier") {
                shouldNotThrowAny {
                    validateVertexAIThinkingConfig(
                        modelIdentifier = VertexAIModelIdentifier.Custom("gemini-exp-1206"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                            ),
                    )
                    validateVertexAIThinkingConfig(
                        modelIdentifier = VertexAIModelIdentifier.Custom("gemini-exp-1206"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                    )
                }
            }

            it("should allow thinkingBudget on a Custom identifier with major version 1") {
                shouldNotThrowAny {
                    validateVertexAIThinkingConfig(
                        modelIdentifier = VertexAIModelIdentifier.Custom("gemini-1.5-pro"),
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                            ),
                    )
                }
            }
        }
    })
