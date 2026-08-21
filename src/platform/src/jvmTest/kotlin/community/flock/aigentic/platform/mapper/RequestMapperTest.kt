package community.flock.aigentic.platform.mapper

import community.flock.aigentic.core.model.GenerationSettings
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
        }
    })
