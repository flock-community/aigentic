package community.flock.aigentic.test

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tokenUsage
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import community.flock.aigentic.platform.dsl.platform
import kotlinx.coroutines.runBlocking

@AigenticParameter
data class QAAnswer(
    val answer: String,
)

fun main(): Unit =
    runBlocking {
        val geminiKey =
            System.getenv("GEMINI_API_KEY")
                ?: error("Set 'GEMINI_API_KEY' environment variable!")
        val platformName =
            System.getenv("AIGENTIC_PLATFORM_NAME")
                ?: error("Set 'AIGENTIC_PLATFORM_NAME' environment variable!")
        val platformSecret =
            System.getenv("AIGENTIC_PLATFORM_SECRET")
                ?: error("Set 'AIGENTIC_PLATFORM_SECRET' environment variable!")

        val qaAgent =
            agent<String, QAAnswer> {
                geminiModel {
                    apiKey(geminiKey)
                    modelIdentifier(GeminiModelIdentifier.Gemini2_0Flash)
                }
                task("Answer general knowledge questions") {
                    addInstruction("Provide a concise one-sentence answer")
                }
                platform {
                    name(platformName)
                    secret(platformSecret)
                }
            }

        val run = qaAgent.start("What is the capital of France?")

        when (val outcome = run.outcome) {
            is Outcome.Finished -> println("AGENT_OUTCOME=Finished answer=${outcome.response?.answer}")
            is Outcome.Stuck -> println("AGENT_OUTCOME=Stuck reason=${outcome.reason}")
            is Outcome.Fatal -> println("AGENT_OUTCOME=Fatal message=${outcome.message}")
        }

        println("TOKEN_USAGE=${run.tokenUsage()}")
    }
