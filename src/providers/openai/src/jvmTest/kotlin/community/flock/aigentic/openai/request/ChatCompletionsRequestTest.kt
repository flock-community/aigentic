package community.flock.aigentic.openai.request

import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ChatCompletionsRequestTest :
    DescribeSpec({

        describe("OpenAI Chat Completions Request") {

            it("should set max completion tokens when configured") {
                val generationSettings = GenerationSettings.DEFAULT.copy(maxOutputTokens = 65536)

                createChatCompletionsRequest(
                    emptyList(),
                    emptyList(),
                    OpenAIModelIdentifier.GPT4O,
                    generationSettings,
                ).maxCompletionTokens shouldBe 65536
            }

            it("should omit max completion tokens when not configured") {
                createChatCompletionsRequest(
                    emptyList(),
                    emptyList(),
                    OpenAIModelIdentifier.GPT4O,
                    GenerationSettings.DEFAULT,
                ).maxCompletionTokens shouldBe null
            }
        }
    })
