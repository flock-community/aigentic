package community.flock.aigentic.vertexai.request

import community.flock.aigentic.core.model.GenerationSettings
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.util.Optional

class RequestMapperTest :
    DescribeSpec({

        describe("VertexAI Request Mapper") {

            it("should set max output tokens when configured") {
                val generationSettings = GenerationSettings.DEFAULT.copy(maxOutputTokens = 65536)

                createGenerateConfig(emptyList(), emptyList(), generationSettings, null)
                    .maxOutputTokens() shouldBe Optional.of(65536)
            }

            it("should omit max output tokens when not configured") {
                createGenerateConfig(emptyList(), emptyList(), GenerationSettings.DEFAULT, null)
                    .maxOutputTokens() shouldBe Optional.empty()
            }
        }
    })
