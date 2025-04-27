package community.flock.aigentic.example

import community.flock.aigentic.code.generation.annotations.AigenticParameter
import community.flock.aigentic.code.generation.annotations.AigenticResponse
import community.flock.aigentic.core.Aigentic
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import community.flock.aigentic.generated.parameter.initialize
import kotlinx.coroutines.runBlocking

@AigenticParameter
data class KotlinMessage(val message: String)

@AigenticResponse
data class MessageSendResult(val result: String)

fun main(): Unit =
    runBlocking {
        Aigentic.initialize()

        val run: Run =
            agent {
                geminiModel {
                    apiKey(geminiKey)
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

        when (val result = run.result) {
            is Result.Finished -> "Agent finished successfully"
            is Result.Stuck -> "Agent is stuck and could not complete task, it says: ${result.reason}"
            is Result.Fatal -> "Agent crashed: ${result.message}"
        }.also(::println)
    }
