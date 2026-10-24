package community.flock.aigentic.gemini.model

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.ThinkingConfig
import community.flock.aigentic.core.model.ThinkingLevel
import community.flock.aigentic.gemini.client.GeminiClient
import community.flock.aigentic.gemini.client.model.Candidate
import community.flock.aigentic.gemini.client.model.Content
import community.flock.aigentic.gemini.client.model.GenerateContentRequest
import community.flock.aigentic.gemini.client.model.GenerateContentResponse
import community.flock.aigentic.gemini.client.model.MediaResolution
import community.flock.aigentic.gemini.client.model.MediaResolutionLevel
import community.flock.aigentic.gemini.client.model.Part
import community.flock.aigentic.gemini.client.model.Role
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot

class GeminiModelTest :
    DescribeSpec({

        describe("GeminiModelIdentifier") {

            it("should expose the correct wire identifiers") {
                GeminiModelIdentifier.Gemini3_8Flash.stringValue shouldBe "gemini-3.8-flash"
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

            it("should send the configured media resolution on attachments") {
                val request = slot<GenerateContentRequest>()
                val geminiClient = mockk<GeminiClient>()
                coEvery { geminiClient.generateContent(capture(request), any()) } returns
                    GenerateContentResponse(candidates = listOf(Candidate(Content(Role.Model, listOf(Part.Text("ok"))))))

                GeminiModel(
                    authentication = Authentication.APIKey("key"),
                    modelIdentifier = GeminiModelIdentifier.Gemini3_8Flash,
                    generationSettings = GenerationSettings.DEFAULT,
                    geminiClient = geminiClient,
                    mediaResolution = GeminiMediaResolution.High,
                ).sendRequest(
                    listOf(Message.Base64(Sender.Agent, "JVBERi0=", MimeType.PDF, MessageCategory.EXECUTION)),
                    emptyList(),
                    null,
                )

                request.captured.contents
                    .single()
                    .parts
                    .single()
                    .shouldBeInstanceOf<Part.Blob>()
                    .mediaResolution shouldBe MediaResolution(MediaResolutionLevel.MEDIA_RESOLUTION_HIGH)
            }

            it("should throw when built with a media resolution on a Gemini 2.x model") {
                shouldThrow<Exception> {
                    GeminiModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = GeminiModelIdentifier.Gemini2_5Flash,
                        generationSettings = GenerationSettings.DEFAULT,
                        geminiClient = mockk<GeminiClient>(relaxed = true),
                        mediaResolution = GeminiMediaResolution.High,
                    )
                }.message shouldContain "mediaResolution is not supported on Gemini 2.x models"
            }

            it("should throw when a media resolution is configured on a Custom gemini-2.x identifier") {
                shouldThrow<Exception> {
                    validateGeminiMediaResolution(GeminiModelIdentifier.Custom("models/gemini-2.5-flash"), GeminiMediaResolution.Low)
                }.message shouldContain "mediaResolution is not supported on Gemini 2.x models"
            }

            it("should allow a media resolution on Gemini 3.x and Custom identifiers without a 2.x version") {
                shouldNotThrowAny {
                    validateGeminiMediaResolution(GeminiModelIdentifier.Gemini3_1FlashLite, GeminiMediaResolution.UltraHigh)
                    validateGeminiMediaResolution(GeminiModelIdentifier.Custom("gemini-4.0-pro"), GeminiMediaResolution.UltraHigh)
                    validateGeminiMediaResolution(GeminiModelIdentifier.Custom("my-proxy"), GeminiMediaResolution.UltraHigh)
                }
            }

            it("should allow no media resolution on a Gemini 2.x model") {
                shouldNotThrowAny {
                    validateGeminiMediaResolution(GeminiModelIdentifier.Gemini2_5Flash, null)
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
