package community.flock.aigentic.koog

import community.flock.aigentic.core.agent.Instruction
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.agent.Task
import community.flock.aigentic.core.platform.Platform
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AigenticConfigTest :
    DescribeSpec({

        describe("AigenticConfig.platform") {

            it("returns the explicitly set platform without ever constructing a default") {
                // No AIGENTIC_PLATFORM_* env vars are set in this test JVM, so if the default
                // (which requires them) were constructed anyway - even just to be discarded - this
                // would throw instead of returning the mock.
                val config = AigenticConfig()
                val explicit = mockk<Platform>()

                config.platform = explicit

                config.platform shouldBe explicit
            }
        }

        describe("AigenticConfig.outputSerializer") {

            it("defaults to a serializer that round-trips a plain String") {
                val config = AigenticConfig()

                Json.encodeToString(config.outputSerializer, "hello") shouldBe "\"hello\""
            }

            it("can be overridden directly for structured output") {
                val config = AigenticConfig()

                @Suppress("UNCHECKED_CAST")
                config.outputSerializer = Int.serializer() as kotlinx.serialization.KSerializer<Any>

                Json.encodeToString(config.outputSerializer, 42) shouldBe "42"
            }

            it("can be set via the outputType<Output>() convenience helper") {
                val config = AigenticConfig()

                config.outputType<Int>()

                Json.encodeToString(config.outputSerializer, 42) shouldBe "42"
            }
        }

        describe("AigenticConfig.task") {

            it("throws when read before being set") {
                val config = AigenticConfig()

                shouldThrow<UninitializedPropertyAccessException> { config.task }
            }

            it("returns the explicitly set task") {
                val config = AigenticConfig()
                val task = Task(description = "Answer questions", instructions = listOf(Instruction("Be concise")))

                config.task = task

                config.task shouldBe task
            }
        }

        describe("AigenticConfig defaults") {

            it("tags, exampleRunIds default to empty and onRunReported defaults to a no-op") {
                val config = AigenticConfig()

                config.tags shouldBe emptyList<RunTag>()
                config.exampleRunIds shouldBe emptyList<RunId>()
                config.onRunReported(RunId("run-1"))
            }
        }
    })
