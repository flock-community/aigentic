package community.flock.aigentic.initializr

import community.flock.aigentic.core.Aigentic
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.annotations.AigenticResponse
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import community.flock.aigentic.generated.parameter.initialize

@AigenticResponse
@AigenticParameter
data class Answer(val answer: String)

suspend fun main() {
    Aigentic.initialize()

    val agent =
        agent<String, Answer> {
            // Configure the model for the agent
            geminiModel {
                apiKey("YOUR_API_KEY")
                modelIdentifier(GeminiModelIdentifier.Gemini2_5FlashPreview)
            }

            // Configure the task for the agent
            task("Answer questions about Kotlin Multiplatform") {
                addInstruction("Provide concise and accurate answers")
            }
        }

    // Start the agent and get a run
    val run = agent.start("What is cool about kotlin?")

    // Print the result
    when (val result = run.result) {
        is Result.Finished -> println(result.response?.answer)
        is Result.Stuck -> println("Agent is stuck: ${result.reason}")
        is Result.Fatal -> println("Error: ${result.message}")
    }
}
