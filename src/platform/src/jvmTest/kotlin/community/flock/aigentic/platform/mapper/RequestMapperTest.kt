package community.flock.aigentic.platform.mapper

import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.ThinkingConfig
import community.flock.aigentic.core.model.ThinkingLevel
import community.flock.aigentic.platform.util.createAgent
import community.flock.aigentic.platform.util.createAgentRun
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.serializer

class RequestMapperTest :
    DescribeSpec({

        describe("Request Mapper") {

            it("should map max output tokens onto the config when configured") {
                val agent = createAgent(GenerationSettings.DEFAULT.copy(maxOutputTokens = 65536))

                createAgentRun()
                    .toDto(agent, serializer<String>())
                    .config.maxOutputTokens shouldBe 65536L
            }

            it("should leave max output tokens empty when not configured") {
                val agent = createAgent()

                createAgentRun()
                    .toDto(agent, serializer<String>())
                    .config.maxOutputTokens shouldBe null
            }

            it("should map the model's media resolution onto the config when configured") {
                val agent = createAgent(mediaResolution = "HIGH")

                createAgentRun()
                    .toDto(agent, serializer<String>())
                    .config.mediaResolution shouldBe "HIGH"
            }

            it("should leave the media resolution empty when not configured") {
                createAgentRun()
                    .toDto(createAgent(), serializer<String>())
                    .config.mediaResolution shouldBe null
            }

            it("should map the thinking level onto the config when configured") {
                val agent = createAgent(GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW)))

                createAgentRun()
                    .toDto(agent, serializer<String>())
                    .config.thinkingLevel shouldBe "LOW"
            }

            it("should leave the thinking level empty when a thinking budget is configured") {
                val agent = createAgent(GenerationSettings.DEFAULT.copy(thinkingConfig = ThinkingConfig(thinkingBudget = 1024)))

                createAgentRun()
                    .toDto(agent, serializer<String>())
                    .config
                    .run { thinkingLevel to thinkingBudget } shouldBe (null to 1024L)
            }

            it("should default temperature to null when not configured") {
                val agent = createAgent()

                createAgentRun()
                    .toDto(agent, serializer<String>())
                    .config.temperature shouldBe null
            }

            it("should map temperature onto the config when configured") {
                val agent = createAgent(GenerationSettings.DEFAULT.copy(temperature = 0.5f))

                createAgentRun()
                    .toDto(agent, serializer<String>())
                    .config.temperature shouldBe 0.5
            }
        }
    })
