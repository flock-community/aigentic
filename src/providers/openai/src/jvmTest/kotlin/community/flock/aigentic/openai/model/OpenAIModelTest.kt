package community.flock.aigentic.openai.model

import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.ThinkingConfig
import community.flock.aigentic.core.model.ThinkingLevel
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

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
                }
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
        }
    })
