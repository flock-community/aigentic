package community.flock.aigentic.openai.request

import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ChatCompletionsRequestTest :
    DescribeSpec({

        val generationSettings = GenerationSettings.DEFAULT.copy(maxOutputTokens = 65536)

        describe("OpenAI Chat Completions Request") {

            it("should send max completion tokens for reasoning models") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.O1,
                        generationSettings,
                    )

                request.maxCompletionTokens shouldBe 65536
                request.maxTokens shouldBe null
            }

            it("should send max completion tokens for the gpt-5 family") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.GPT5,
                        generationSettings,
                    )

                request.maxCompletionTokens shouldBe 65536
                request.maxTokens shouldBe null
            }

            it("should send max tokens for classic models") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.GPT4O,
                        generationSettings,
                    )

                request.maxTokens shouldBe 65536
                request.maxCompletionTokens shouldBe null
            }

            it("should send max tokens for custom models") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.Custom("some-ollama-model"),
                        generationSettings,
                    )

                request.maxTokens shouldBe 65536
                request.maxCompletionTokens shouldBe null
            }

            it("should omit both token limits when not configured") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.GPT4O,
                        GenerationSettings.DEFAULT,
                    )

                request.maxTokens shouldBe null
                request.maxCompletionTokens shouldBe null
            }
        }
    })
