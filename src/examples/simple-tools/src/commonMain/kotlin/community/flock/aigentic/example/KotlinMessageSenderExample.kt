package community.flock.aigentic.example

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.annotations.AigenticResponse
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import kotlinx.serialization.Serializable

@Serializable
data class KotlinMessage(val message: String)

@AigenticResponse
data class MessageSendResult(val result: String)

suspend fun runKotlinMessageAgent(apiKey: String) {
    val run =
        agent {
            geminiModel {
                apiKey(apiKey)
                modelIdentifier(GeminiModelIdentifier.Gemini2_0Flash)
            }
            task("Send 2 nice messages about Kotlin") {
                addInstruction("use the sendMessageTool to send an individual message")
                addInstruction("After the message has been send you're finished")
            }
            addTool("sendMessageTool") { input: KotlinMessage ->
                MessageSendResult("Sent successfully: ${input.message}")
            }
        }.start()

    when (val result = run.outcome) {
        is Outcome.Finished -> "Agent finished successfully"
        is Outcome.Stuck -> "Agent is stuck and could not complete task, it says: ${result.reason}"
        is Outcome.Fatal -> "Agent crashed: ${result.message}"
    }.also(::println)
}
