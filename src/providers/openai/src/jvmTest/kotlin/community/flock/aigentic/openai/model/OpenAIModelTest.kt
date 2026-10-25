package community.flock.aigentic.openai.model

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ThinkingConfig
import community.flock.aigentic.core.model.ThinkingLevel
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain

private object OtherModelIdentifier : ModelIdentifier {
    override val stringValue: String = "other-model"
}

class OpenAIModelTest :
    DescribeSpec({

        describe("OpenAIModel") {

            it("should throw when built with an invalid thinking configuration") {
                shouldThrow<Exception> {
                    OpenAIModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = OpenAIModelIdentifier.GPT4O,
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                        apiUrl = OpenAIApiUrl("https://api.openai.com/v1/"),
                    )
                }.message shouldContain "it is not a reasoning model"
            }

            it("should build successfully with thinkingLevel MINIMAL on O3") {
                shouldNotThrowAny {
                    OpenAIModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = OpenAIModelIdentifier.O3,
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL),
                            ),
                        apiUrl = OpenAIApiUrl("https://api.openai.com/v1/"),
                    )
                }
            }

            it("should build successfully with a valid thinking configuration") {
                shouldNotThrowAny {
                    OpenAIModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = OpenAIModelIdentifier.O3,
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                        apiUrl = OpenAIApiUrl("https://api.openai.com/v1/"),
                    )
                }
            }

            it("should not validate an unrelated ModelIdentifier implementation") {
                shouldNotThrowAny {
                    validateOpenAIThinkingConfig(
                        modelIdentifier = OtherModelIdentifier,
                        generationSettings =
                            GenerationSettings.DEFAULT.copy(
                                thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                            ),
                    )
                }
            }

            it("should throw before sending a request when a PDF attachment is passed") {
                val model =
                    OpenAIModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = OpenAIModelIdentifier.GPT5_5,
                        generationSettings = GenerationSettings.DEFAULT,
                        apiUrl = OpenAIApiUrl("http://127.0.0.1:9/v1/"),
                    )

                shouldThrow<Exception> {
                    model.sendRequest(
                        listOf(Message.Base64(Sender.Agent, "JVBERi0=", MimeType.PDF, MessageCategory.RUN_CONTEXT)),
                        emptyList(),
                        null,
                    )
                }.message shouldContain "PDF attachments are not supported on OpenAI-compatible models"
            }

            it("should throw when built with imageDetail ORIGINAL on a model older than gpt-5.4") {
                shouldThrow<Exception> {
                    OpenAIModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = OpenAIModelIdentifier.GPT4O,
                        generationSettings = GenerationSettings.DEFAULT,
                        apiUrl = OpenAIApiUrl("https://api.openai.com/v1/"),
                        imageDetail = OpenAIImageDetail.ORIGINAL,
                    )
                }.message shouldContain "imageDetail ORIGINAL is only supported on gpt-5.4 and newer models"
            }

            it("should build successfully with imageDetail ORIGINAL on gpt-5.4") {
                shouldNotThrowAny {
                    OpenAIModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = OpenAIModelIdentifier.GPT5_4,
                        generationSettings = GenerationSettings.DEFAULT,
                        apiUrl = OpenAIApiUrl("https://api.openai.com/v1/"),
                        imageDetail = OpenAIImageDetail.ORIGINAL,
                    )
                }
            }

            it("should allow every image detail except ORIGINAL on a model older than gpt-5.4") {
                shouldNotThrowAny {
                    listOf(OpenAIImageDetail.AUTO, OpenAIImageDetail.LOW, OpenAIImageDetail.HIGH).forEach {
                        validateOpenAIImageDetail(OpenAIModelIdentifier.GPT4O, it)
                    }
                }
            }

            it("should not validate the image detail of a Custom or unrelated ModelIdentifier") {
                shouldNotThrowAny {
                    validateOpenAIImageDetail(OpenAIModelIdentifier.Custom("my-model"), OpenAIImageDetail.ORIGINAL)
                    validateOpenAIImageDetail(OtherModelIdentifier, OpenAIImageDetail.ORIGINAL)
                }
            }

            it("should throw when built with an explicit temperature on a reasoning model") {
                shouldThrow<Exception> {
                    OpenAIModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = OpenAIModelIdentifier.O3,
                        generationSettings = GenerationSettings.DEFAULT.copy(temperature = 0.5f),
                        apiUrl = OpenAIApiUrl("https://api.openai.com/v1/"),
                    )
                }.message shouldContain "temperature is not supported"
            }

            it("should throw when built with an explicit topP on a reasoning model") {
                shouldThrow<Exception> {
                    OpenAIModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = OpenAIModelIdentifier.O3,
                        generationSettings = GenerationSettings.DEFAULT.copy(topP = 0.5f),
                        apiUrl = OpenAIApiUrl("https://api.openai.com/v1/"),
                    )
                }.message shouldContain "topP is not supported"
            }

            it("should build successfully with a non-reasoning model and an explicit temperature") {
                shouldNotThrowAny {
                    OpenAIModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = OpenAIModelIdentifier.GPT4O,
                        generationSettings = GenerationSettings.DEFAULT.copy(temperature = 0.5f),
                        apiUrl = OpenAIApiUrl("https://api.openai.com/v1/"),
                    )
                }
            }

            it("should not throw when built with topK on a reasoning model") {
                shouldNotThrowAny {
                    OpenAIModel(
                        authentication = Authentication.APIKey("key"),
                        modelIdentifier = OpenAIModelIdentifier.O3,
                        generationSettings = GenerationSettings.DEFAULT.copy(topK = 40),
                        apiUrl = OpenAIApiUrl("https://api.openai.com/v1/"),
                    )
                }
            }
        }
    })
