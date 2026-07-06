package community.flock.aigentic.example.koog

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.agents.ext.agent.structuredOutputWithToolsStrategy
import ai.koog.http.client.HttpClientFactoryResolver
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.openai.base.structure.OpenAIStandardJsonSchemaGenerator
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.structure.StructuredRequest
import ai.koog.prompt.structure.StructuredRequestConfig
import ai.koog.prompt.structure.json.JsonStructure
import community.flock.aigentic.core.agent.Instruction
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.agent.Task
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.koog.fetchExampleRunPrompt
import community.flock.aigentic.koog.reportRunsToAigentic
import community.flock.aigentic.platform.AigenticPlatform
import community.flock.aigentic.platform.client.defaultPlatformApiUrl
import kotlinx.serialization.Serializable

class WeatherTools : ToolSet {
    @Tool
    @LLMDescription("Returns the current weather for a city")
    fun getWeather(city: String): String = "It's sunny and 21°C in $city"
}

@Serializable
@LLMDescription("Answer to a weather question")
data class WeatherAnswer(
    @property:LLMDescription("The answer to the user's weather question")
    val answer: String,
)

private val openAIKey by lazy {
    System.getenv("OPENAI_API_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'OPENAI_API_KEY' environment variable!")
    }
}

private val aigenticPlatformSecret by lazy {
    System.getenv("AIGENTIC_PLATFORM_SECRET").also {
        if (it.isNullOrEmpty()) error("Set 'AIGENTIC_PLATFORM_SECRET' environment variable!")
    }
}

private val aigenticPlatformName by lazy { System.getenv("AIGENTIC_PLATFORM_NAME") ?: "koog-example" }

private val aigenticPlatformUrl by lazy { System.getenv("AIGENTIC_PLATFORM_URL") ?: defaultPlatformApiUrl }

private val systemPrompt by lazy {
    System.getenv("KOOG_EXAMPLE_SYSTEM_PROMPT")
        ?: "You are a weather assistant. Use the weather tool to answer questions about the weather."
}

suspend fun main() {
    val platform =
        AigenticPlatform(
            authentication = Authentication.BasicAuth(username = aigenticPlatformName, password = aigenticPlatformSecret),
            apiUrl = PlatformApiUrl(aigenticPlatformUrl),
        )

    val tags = listOf(RunTag("koog-example"))

    val toolRegistry = ToolRegistry { tools(WeatherTools().asTools()) }

    val openAIClient = OpenAILLMClient(apiKey = openAIKey, httpClientFactory = HttpClientFactoryResolver.resolve())

    // Native mode sets the request's schema parameter instead of adding a prompting message, so it
    // doesn't shift what AigenticReporting captures as the RUN_CONTEXT message (the first user turn).
    val structuredOutputConfig =
        StructuredRequestConfig(
            default = StructuredRequest.Native(JsonStructure.create<WeatherAnswer>(schemaGenerator = OpenAIStandardJsonSchemaGenerator)),
        )

    MultiLLMPromptExecutor(mapOf(LLMProvider.OpenAI to openAIClient)).use { executor ->
        val (prompt, exampleRunIds) = fetchExampleRunPrompt<WeatherAnswer>(platform, tags, systemPrompt)

        val agent =
            AIAgent(
                promptExecutor = executor,
                agentConfig = AIAgentConfig(prompt = prompt, model = OpenAIModels.Chat.GPT4oMini, maxAgentIterations = 50),
                strategy = structuredOutputWithToolsStrategy<WeatherAnswer>(structuredOutputConfig),
                toolRegistry = toolRegistry,
            ) {
                reportRunsToAigentic<WeatherAnswer>(
                    platform = platform,
                    task =
                        Task(
                            description = "Answer weather questions",
                            instructions = listOf(Instruction("Use the weather tool for current conditions")),
                        ),
                    tags = tags,
                    exampleRunIds = exampleRunIds,
                )
            }

        println(agent.run("What's the weather like in Amsterdam?"))
    }
}
