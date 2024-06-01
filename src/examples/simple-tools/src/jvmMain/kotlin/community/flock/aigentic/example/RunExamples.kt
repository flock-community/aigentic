package community.flock.aigentic.example

import kotlinx.coroutines.runBlocking

private val openAIAPIKey =
    System.getenv("OPENAI_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'OPENAI_KEY' environment variable!")
    }

private val geminiKey =
    System.getenv("GEMINI_API_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'GEMINI_API_KEY' environment variable!")
    }

fun main(): Unit =
    runBlocking {
//        runAdministrativeAgentExample(openAIAPIKey)
//        runKotlinMessageAgentExample(geminiKey)
        runItemCategorizeExample(geminiKey, FileReader.readFile("/base64Image.txt"))
    }
