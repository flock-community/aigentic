package community.flock.aigentic.openai.model

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
