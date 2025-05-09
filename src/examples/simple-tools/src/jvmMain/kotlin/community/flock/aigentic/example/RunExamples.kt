package community.flock.aigentic.example

import community.flock.aigentic.core.agent.inputTokens
import community.flock.aigentic.core.agent.outputTokens
import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import community.flock.aigentic.ollama.dsl.ollamaModel
import community.flock.aigentic.openai.dsl.openAIModel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
import kotlinx.coroutines.runBlocking

private val openAIAPIKey by lazy {
    System.getenv("OPENAI_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'OPENAI_KEY' environment variable!")
    }
}

private val geminiKey by lazy {
    System.getenv("GEMINI_API_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'GEMINI_API_KEY' environment variable!")
    }
}

// Set the active example and provider here
val activeRunExample = RunExamples.INVOICE_EXTRACTOR_AGENT
val activeProvider = Provider.GEMINI

fun main() {
    runBlocking {
        when (activeRunExample) {
            RunExamples.ADMINISTRATIVE_AGENT -> runAdministrativeAgentExample(AgentConfig::configureModel)
            RunExamples.KOTLIN_MESSAGE_AGENT -> runKotlinMessageAgentExample(AgentConfig::configureModel)
            RunExamples.ITEM_CATEGORIZE_AGENT ->
                runItemCategorizeExample(
                    FileReader.readFileBase64("/table-items.png"),
                    AgentConfig::configureModel,
                )
            /**
             * PDF is currently only supported by Gemini
             */
            RunExamples.INVOICE_EXTRACTOR_AGENT ->
                invoiceExtractorAgent(
                    FileReader.readFileBase64("/test-invoice.pdf"),
                    AgentConfig::configureModel,
                )
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
}

fun AgentConfig.configureModel() {
    when (activeProvider) {
        Provider.GEMINI ->
            geminiModel {
                apiKey(geminiKey)
                modelIdentifier(GeminiModelIdentifier.Gemini2_0Flash)
            }
        Provider.OPENAI ->
            openAIModel {
                apiKey(openAIAPIKey)
                modelIdentifier(OpenAIModelIdentifier.GPT4OMini)
            }
        Provider.OLLAMA ->
            ollamaModel {
                modelIdentifier(
                    object : ModelIdentifier {
                        override val stringValue: String = "llama3.1"
                    },
                )
            }
    }
}

enum class RunExamples {
    ADMINISTRATIVE_AGENT,
    KOTLIN_MESSAGE_AGENT,
    ITEM_CATEGORIZE_AGENT,
    INVOICE_EXTRACTOR_AGENT,
}

enum class Provider {
    GEMINI,
    OPENAI,
    OLLAMA,
}
