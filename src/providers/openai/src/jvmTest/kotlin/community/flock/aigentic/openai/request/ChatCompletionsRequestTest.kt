package community.flock.aigentic.openai.request

import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.ThinkingConfig
import community.flock.aigentic.core.model.ThinkingLevel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

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

            it("should send reasoning effort for reasoning models") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.O1,
                        GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW)),
                    )

                request.reasoningEffort?.id shouldBe "low"
            }

            it("should send reasoning effort for custom models") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.Custom("some-ollama-model"),
                        GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MEDIUM)),
                    )

                request.reasoningEffort?.id shouldBe "medium"
            }

            it("should throw when thinkingLevel is configured on a non-reasoning model") {
                shouldThrow<Exception> {
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.GPT4O,
                        GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW)),
                    )
                }.message shouldContain "it is not a reasoning model"
            }

            it("should throw when thinkingBudget is configured") {
                shouldThrow<Exception> {
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.O1,
                        GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingBudget = 1024)),
                    )
                }.message shouldContain "thinkingBudget is not supported by OpenAI-compatible models"
            }

            it("should omit reasoning effort when not configured") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.O1,
                        GenerationSettings.DEFAULT,
                    )

                request.reasoningEffort shouldBe null
            }

            it("should send reasoning effort low when thinkingLevel MINIMAL is configured on O3") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.O3,
                        GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL)),
                    )

                request.reasoningEffort?.id shouldBe "low"
            }

            it("should send reasoning effort minimal for a gpt-5 model") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.GPT5,
                        GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL)),
                    )

                request.reasoningEffort?.id shouldBe "minimal"
            }

            it("should allow thinkingLevel LOW on O3") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.O3,
                        GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW)),
                    )

                request.reasoningEffort?.id shouldBe "low"
            }

            it("should send reasoning effort low when thinkingLevel MINIMAL is configured on gpt-5.4") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.GPT5_4,
                        GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL)),
                    )

                request.reasoningEffort?.id shouldBe "low"
            }

            it("should send reasoning effort low when thinkingLevel MINIMAL is configured on gpt-5.5") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.GPT5_5,
                        GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL)),
                    )

                request.reasoningEffort?.id shouldBe "low"
            }

            it("should send reasoning effort medium when thinkingLevel MINIMAL is configured on gpt-5.4-pro") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.GPT5_4Pro,
                        GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL)),
                    )

                request.reasoningEffort?.id shouldBe "medium"
            }

            it("should send reasoning effort medium when thinkingLevel LOW is configured on gpt-5.5-pro") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.GPT5_5Pro,
                        GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW)),
                    )

                request.reasoningEffort?.id shouldBe "medium"
            }

            it("should send default temperature and topP for a non-reasoning model when not configured") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.GPT4O,
                        GenerationSettings.DEFAULT,
                    )

                request.temperature shouldBe GenerationSettings.DEFAULT_TEMPERATURE.toDouble()
                request.topP shouldBe GenerationSettings.DEFAULT_TOP_P.toDouble()
            }

            it("should omit temperature and topP for a reasoning model when not configured") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.O3,
                        GenerationSettings.DEFAULT,
                    )

                request.temperature shouldBe null
                request.topP shouldBe null
            }

            it("should omit temperature and topP for a gpt-5 model when not configured") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.GPT5,
                        GenerationSettings.DEFAULT,
                    )

                request.temperature shouldBe null
                request.topP shouldBe null
            }

            it("should send default temperature and topP for a Custom model when not configured") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.Custom("some-ollama-model"),
                        GenerationSettings.DEFAULT,
                    )

                request.temperature shouldBe GenerationSettings.DEFAULT_TEMPERATURE.toDouble()
                request.topP shouldBe GenerationSettings.DEFAULT_TOP_P.toDouble()
            }

            it("should forward an explicit topP for gpt-4o") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.GPT4O,
                        GenerationSettings.DEFAULT.copy(topP = 0.9f),
                    )

                request.topP shouldBe 0.9f.toDouble()
            }

            it("should send an explicit temperature for a Custom model without throwing") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.Custom("llama3"),
                        GenerationSettings.DEFAULT.copy(temperature = 0.2f),
                    )

                request.temperature shouldBe 0.2f.toDouble()
            }

            it("should not send topK for a reasoning model and not throw") {
                val request =
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.O3,
                        GenerationSettings.DEFAULT.copy(topK = 40),
                    )

                request.temperature shouldBe null
            }

            it("should throw when temperature is configured on a reasoning model") {
                shouldThrow<Exception> {
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.O3,
                        GenerationSettings.DEFAULT.copy(temperature = 0.5f),
                    )
                }.message shouldContain "temperature is not supported"
            }

            it("should throw when topP is configured on a reasoning model") {
                shouldThrow<Exception> {
                    createChatCompletionsRequest(
                        emptyList(),
                        emptyList(),
                        OpenAIModelIdentifier.O3,
                        GenerationSettings.DEFAULT.copy(topP = 0.5f),
                    )
                }.message shouldContain "topP is not supported"
            }
        }
    })
