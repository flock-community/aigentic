package community.flock.aigentic.example

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.model.LogLevel
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier

@AigenticParameter
data class Words(val words: List<String>)

suspend fun runStructuredResponseAgent(apiKey: String) {
    val agent =
        agent<String, Words> {
            geminiModel {
                apiKey(apiKey)
                modelIdentifier(GeminiModelIdentifier.Gemini2_5Flash)
                logLevel(LogLevel.DEBUG)
            }
            task("Split the text in its individual words") {
                addInstruction("Omit punctuation marks like dashes, dots and question marks")
            }
        }

    val run = agent.start("This is a sentence with dashes, dots and question marks?.")

    when (val result = run.outcome) {
        is Outcome.Finished -> "Agent finished successfully, words: ${result.response?.words}"
        is Outcome.Stuck -> "Agent is stuck and could not complete task, it says: ${result.reason}"
        is Outcome.Fatal -> "Agent crashed: ${result.message}"
    }.also(::println)
}
