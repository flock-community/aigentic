package community.flock.aigentic.ollama.dsl

import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.model.ThinkingLevel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.mockk

class OllamaDslTest :
    DescribeSpec({

        describe("ollamaModel") {

            it("should build without error when a thinkingLevel is configured") {
                shouldNotThrowAny {
                    agent<Unit, Unit> {
                        ollamaModel {
                            modelIdentifier(OpenAIModelIdentifier.Custom("llama3"))
                            generationConfig {
                                thinkingLevel(ThinkingLevel.LOW)
                            }
                        }
                        task("Task description") {}
                        addTool(mockk(relaxed = true))
                    }
                }
            }

            it("should throw when a thinkingBudget is configured") {
                shouldThrow<Exception> {
                    agent<Unit, Unit> {
                        ollamaModel {
                            modelIdentifier(OpenAIModelIdentifier.Custom("llama3"))
                            generationConfig {
                                thinkingBudget(10)
                            }
                        }
                        task("Task description") {}
                        addTool(mockk(relaxed = true))
                    }
                }
            }
        }
    })
