package community.flock.aigentic.koog

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStructured
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.agents.testing.tools.getMockExecutor
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Serializable
@SerialName("Invoice")
@LLMDescription("An invoice with its line items")
data class Invoice(
    @property:LLMDescription("Invoice number") val number: String,
    @property:LLMDescription("Total amount due") val total: Double,
    @property:LLMDescription("Line items on the invoice") val items: List<LineItem>,
) {
    @Serializable
    @SerialName("LineItem")
    data class LineItem(
        @property:LLMDescription("Description of the item") val description: String,
        @property:LLMDescription("Amount for the item") val amount: Double,
    )
}

class StructuredOutputExporterTest {
    @Test
    fun `structured request-response agent publishes its typed result as a RunDto`() =
        runTest {
            val capturedBodies = mutableListOf<String>()
            val mockEngine =
                MockEngine { request ->
                    capturedBodies += (request.body as TextContent).text
                    respond(content = "", status = HttpStatusCode.Created)
                }
            val httpClient = HttpClient(mockEngine) { install(ContentNegotiation) { json(aigenticJson) } }

            val invoiceJson =
                """{"number":"INV-42","total":42.0,"items":[{"description":"Widget","amount":42.0}]}"""

            val mockExecutor = getMockExecutor { mockLLMAnswer(invoiceJson).asDefaultResponse }

            val model =
                LLModel(
                    provider = LLMProvider.OpenAI,
                    id = "gpt-4o",
                    capabilities = listOf(LLMCapability.Completion, LLMCapability.Temperature),
                )

            val extractInvoice =
                strategy<String, Invoice>("invoice-extraction") {
                    val extract by nodeLLMRequestStructured<Invoice>()
                    edge(nodeStart forwardTo extract)
                    edge(extract forwardTo nodeFinish transformed { it.getOrThrow().data })
                }

            val agent =
                AIAgent(
                    promptExecutor = mockExecutor,
                    llmModel = model,
                    strategy = extractInvoice,
                    systemPrompt = "Extract the structured invoice from the document.",
                    temperature = 0.0,
                ) {
                    aigenticPlatform(
                        name = "user",
                        secret = "secret",
                        apiUrl = "https://platform.test/",
                        httpClient = httpClient,
                    )
                }

            val invoice: Invoice = agent.run("<invoice document>")

            assertEquals("INV-42", invoice.number)
            assertEquals("Widget", invoice.items.single().description)

            val run = aigenticJson.decodeFromString<RunDto>(capturedBodies.single())

            val result = run.result
            assertTrue(result is FinishedResultDto, "Structured run should finish successfully")
            assertTrue(result.response!!.contains("INV-42"), "The structured output should be published as the run result")
            assertTrue(
                run.messages.any { it is TextMessageDto && it.sender == SenderDto.Model && it.text.contains("INV-42") },
                "The structured JSON should appear as the assistant message",
            )
        }
}
