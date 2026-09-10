package community.flock.aigentic.koog

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.testing.tools.getMockExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AigenticPlatformExporterTest {
    @Test
    fun `koog agent run is published to the aigentic platform as a RunDto`() =
        runTest {
            val capturedPaths = mutableListOf<String>()
            val capturedBodies = mutableListOf<String>()

            val mockEngine =
                MockEngine { request ->
                    capturedPaths += request.url.encodedPath
                    capturedBodies += (request.body as TextContent).text
                    respond(content = "", status = HttpStatusCode.Created)
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) { json(aigenticJson) }
                }

            val mockExecutor =
                getMockExecutor {
                    mockLLMAnswer("The answer is 42.").asDefaultResponse
                }

            val model =
                LLModel(
                    provider = LLMProvider.OpenAI,
                    id = "gpt-4o",
                    capabilities = listOf(LLMCapability.Completion, LLMCapability.Tools, LLMCapability.Temperature),
                )

            val agent =
                AIAgent(
                    promptExecutor = mockExecutor,
                    llmModel = model,
                    systemPrompt = "You are a helpful assistant.",
                    temperature = 0.0,
                ) {
                    aigenticPlatform(
                        name = "user",
                        secret = "secret",
                        apiUrl = "https://platform.test/",
                        httpClient = httpClient,
                    )
                }

            agent.run("What is the answer?")

            assertEquals(1, capturedPaths.size, "Expected exactly one run to be published")
            assertEquals("/gateway/runs", capturedPaths.single())

            val run = aigenticJson.decodeFromString<RunDto>(capturedBodies.single())

            assertEquals("gpt-4o", run.config.modelIdentifier)
            assertEquals("You are a helpful assistant.", run.config.systemPrompt)
            assertTrue(run.messages.any { it is SystemPromptMessageDto }, "Run should contain the system prompt message")
            assertTrue(
                run.messages.any { it is TextMessageDto && it.sender == SenderDto.Model && it.text.contains("42") },
                "Run should contain the assistant's response",
            )
            assertTrue(run.modelRequests.isNotEmpty(), "Run should record at least one model request")

            val result = run.result
            assertTrue(result is FinishedResultDto, "Run should finish successfully")
            assertEquals("The answer is 42.", result.response)
        }
}
