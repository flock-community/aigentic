package community.flock.aigentic.example

import kotlinx.coroutines.runBlocking


private val openAIAPIKey = System.getenv("OPENAI_KEY").also {
    if (it.isNullOrEmpty()) error("Set 'OPENAI_KEY' environment variable!")
}

fun main(): Unit = runBlocking {
    runAdministrativeAgentExample(openAIAPIKey)
}
