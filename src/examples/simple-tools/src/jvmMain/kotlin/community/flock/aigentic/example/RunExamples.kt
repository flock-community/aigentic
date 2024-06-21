package community.flock.aigentic.example

import community.flock.aigentic.core.agent.inputTokens
import community.flock.aigentic.core.agent.outputTokens
import community.flock.aigentic.core.model.Authentication.APIKey
import community.flock.aigentic.example.Provider.GEMINI
import community.flock.aigentic.example.Provider.OPENAI
import community.flock.aigentic.gemini.model.GeminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import community.flock.aigentic.openai.model.OpenAIModel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
import kotlinx.coroutines.runBlocking

private val openAIAPIKey =
    System.getenv("OPENAI_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'OPENAI_KEY' environment variable!")
    }

private val geminiKey =
    System.getenv("GEMINI_API_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'GEMINI_API_KEY' environment variable!")
    }

// Set the active example and provider here
val activeRunExample = RunExamples.ITEM_CATEGORIZE_AGENT
val activeProvider = OPENAI

fun main(): Unit =
    runBlocking {
        val model =
            when (activeProvider) {
                GEMINI -> GeminiModel(APIKey(geminiKey), GeminiModelIdentifier.Gemini1_5FlashLatest)
                OPENAI -> OpenAIModel(APIKey(openAIAPIKey), OpenAIModelIdentifier.GPT4O)
            }

        when (activeRunExample) {
            RunExamples.ADMINISTRATIVE_AGENT -> runAdministrativeAgentExample(model)
            RunExamples.KOTLIN_MESSAGE_AGENT -> runKotlinMessageAgentExample(model)
            RunExamples.ITEM_CATEGORIZE_AGENT -> runItemCategorizeExample(model, FileReader.readFile("/base64Image.txt"))
        }.also {
            println(
                """
                Took ${it.finishedAt - it.startedAt}
                Input token count: ${it.inputTokens()}
                Output tokens count: ${it.outputTokens()}
                """.trimIndent(),
            )
        }
    }

enum class RunExamples {
    ADMINISTRATIVE_AGENT,
    KOTLIN_MESSAGE_AGENT,
    ITEM_CATEGORIZE_AGENT,
}

enum class Provider {
    GEMINI,
    OPENAI,
}
