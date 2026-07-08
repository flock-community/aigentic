package community.flock.aigentic.koog

import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.Prompt
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.params.LLMParams
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import java.time.Instant

const val DEFAULT_PLATFORM_API_URL: String = "https://aigentic-backend-kib53ypjwq-ez.a.run.app/"

/**
 * Installs an [ai.koog.agents.features.eventHandler.feature.EventHandler] that captures the
 * agent run and publishes it to the Aigentic Platform (`POST RunDto -> /gateway/runs`) on completion.
 *
 * This is the Koog-side equivalent of Aigentic's automatic `publishRun` call: it reuses Koog's
 * public `handleEvents` hooks to collect everything the platform contract needs.
 */
fun GraphAIAgent.FeatureContext.aigenticPlatform(
    name: String,
    secret: String,
    apiUrl: String = DEFAULT_PLATFORM_API_URL,
    httpClient: HttpClient = defaultAigenticHttpClient(),
    agentDescription: String = "Koog agent",
) {
    val collector = RunCollector()
    handleEvents {
        onAgentStarting { collector.onStart(it.runId, it.agent.agentConfig.prompt.messages.size) }
        onLLMCallStarting { collector.onLlmStart(it.runId) }
        onLLMCallCompleted { collector.onLlmCompleted(it.runId, it.prompt, it.model, it.tools, it.response) }
        onAgentCompleted { event ->
            val structuredOutput = event.result != null && event.result !is String
            val run =
                collector.build(event.runId, agentDescription, structuredOutput) { responseText ->
                    FinishedResultDto("Agent completed", responseText ?: event.result?.toString())
                }
            if (run != null) httpClient.sendRun(apiUrl, name, secret, run)
        }
        onAgentExecutionFailed { event ->
            val run =
                collector.build(event.runId, agentDescription, structuredOutput = false) {
                    FatalResultDto(event.error.message ?: "Unknown error")
                }
            if (run != null) httpClient.sendRun(apiUrl, name, secret, run)
        }
    }
}

fun defaultAigenticHttpClient(): HttpClient =
    HttpClient {
        install(ContentNegotiation) { json(aigenticJson) }
    }

private suspend fun HttpClient.sendRun(
    apiUrl: String,
    name: String,
    secret: String,
    run: RunDto,
) {
    post(apiUrl.trimEnd('/') + "/gateway/runs") {
        contentType(ContentType.Application.Json)
        basicAuth(name, secret)
        setBody(run)
    }
}

private class RunCollector {
    private val runs = mutableMapOf<String, Accumulator>()

    fun onStart(
        runId: String,
        configPromptSize: Int,
    ) {
        runs[runId] = Accumulator(startedAt = now(), configPromptSize = configPromptSize)
    }

    fun onLlmStart(runId: String) {
        accumulator(runId).llmCallStarts.addLast(now())
    }

    fun onLlmCompleted(
        runId: String,
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>,
        response: Message.Assistant?,
    ) {
        val accumulator = accumulator(runId)
        accumulator.model = model
        accumulator.tools = tools
        accumulator.temperature = prompt.params.temperature
        accumulator.messages = prompt.messages + listOfNotNull(response)
        accumulator.lastResponseText = response?.textContent()
        (prompt.params.schema as? LLMParams.Schema.JSON)?.let { accumulator.responseSchemaJson = it.schema.toString() }
        val meta = response?.metaInfo
        accumulator.modelRequests +=
            ModelRequestInfoDto(
                startedAt = accumulator.llmCallStarts.removeFirstOrNull() ?: now(),
                finishedAt = now(),
                inputTokenCount = meta?.inputTokensCount ?: 0,
                outputTokenCount = meta?.outputTokensCount ?: 0,
            )
    }

    fun build(
        runId: String,
        agentDescription: String,
        structuredOutput: Boolean,
        result: (lastResponseText: String?) -> ResultDto,
    ): RunDto? {
        val accumulator = runs.remove(runId) ?: return null
        val systemPrompt = accumulator.messages.filterIsInstance<Message.System>().firstOrNull()?.textContent() ?: ""
        return RunDto(
            startedAt = accumulator.startedAt,
            finishedAt = now(),
            config =
                ConfigDto(
                    task = TaskDto(agentDescription, emptyList()),
                    modelIdentifier = accumulator.model?.id ?: "unknown",
                    systemPrompt = systemPrompt,
                    tools = accumulator.tools.toToolDtos(),
                    responseJsonSchema = accumulator.responseSchemaJson,
                    temperature = accumulator.temperature ?: 0.0,
                ),
            result = result(accumulator.lastResponseText),
            messages =
                accumulator.messages.toMessageDtos(
                    configPromptSize = accumulator.configPromptSize,
                    structuredFinalResponse = structuredOutput,
                ),
            modelRequests = accumulator.modelRequests,
        )
    }

    private fun accumulator(runId: String): Accumulator = runs.getOrPut(runId) { Accumulator(now()) }

    private fun now(): String = Instant.now().toString()

    private class Accumulator(
        val startedAt: String,
        val configPromptSize: Int = 0,
        var model: LLModel? = null,
        var tools: List<ToolDescriptor> = emptyList(),
        var temperature: Double? = null,
        var messages: List<Message> = emptyList(),
        var lastResponseText: String? = null,
        var responseSchemaJson: String? = null,
        val modelRequests: MutableList<ModelRequestInfoDto> = mutableListOf(),
        val llmCallStarts: ArrayDeque<String> = ArrayDeque(),
    )
}
