package community.flock.aigentic.koog

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.testing.tools.getMockExecutor
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.AttachmentContent
import ai.koog.prompt.message.AttachmentSource
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.structure.StructuredResponse
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
    fun `pdf invoice is extracted as structured output and published with feature parity`() =
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

            val pdfBytes = "%PDF-1.4 fake invoice bytes".encodeToByteArray()

            val extractInvoice =
                strategy<ByteArray, Invoice>("invoice-extraction") {
                    val extract by node<ByteArray, Result<StructuredResponse<Invoice>>>("extract") { pdf ->
                        llm.writeSession {
                            appendPrompt {
                                user(
                                    listOf(
                                        MessagePart.Text("Extract the invoice from the attached document."),
                                        MessagePart.Attachment(
                                            AttachmentSource.File(AttachmentContent.Binary.Bytes(pdf), format = "pdf", mimeType = "application/pdf"),
                                        ),
                                    ),
                                )
                            }
                            requestLLMStructured<Invoice>()
                        }
                    }
                    edge(nodeStart forwardTo extract)
                    edge(extract forwardTo nodeFinish transformed { it.getOrThrow().data })
                }

            // A reference document attached at agent-definition time (Aigentic's `context { }` => CONFIG_CONTEXT).
            val agentConfig =
                AIAgentConfig(
                    prompt =
                        prompt("invoice-agent") {
                            system("Extract the structured invoice from the document.")
                            user {
                                attachment(AttachmentSource.Image(AttachmentContent.URL("https://example.com/reference-invoice.png"), format = "png"))
                            }
                        },
                    model = model,
                    maxAgentIterations = 5,
                )

            val agent =
                AIAgent(
                    promptExecutor = mockExecutor,
                    strategy = extractInvoice,
                    agentConfig = agentConfig,
                ) {
                    aigenticPlatform(
                        name = "user",
                        secret = "secret",
                        apiUrl = "https://platform.test/",
                        httpClient = httpClient,
                    )
                }

            val invoice: Invoice = agent.run(pdfBytes)

            assertEquals("INV-42", invoice.number)
            assertEquals("Widget", invoice.items.single().description)

            val run = aigenticJson.decodeFromString<RunDto>(capturedBodies.single())

            // The run's PDF (attached during execution from the run input) => RUN_CONTEXT.
            val pdfMessage = run.messages.filterIsInstance<Base64MessageDto>().single()
            assertEquals(MimeTypeDto.APPLICATION_PDF, pdfMessage.mimeType)
            assertEquals(MessageCategoryDto.RUN_CONTEXT, pdfMessage.category)

            // The reference image from the config prompt => CONFIG_CONTEXT.
            val referenceImage = run.messages.filterIsInstance<UrlMessageDto>().single()
            assertEquals(MimeTypeDto.IMAGE_PNG, referenceImage.mimeType)
            assertEquals(MessageCategoryDto.CONFIG_CONTEXT, referenceImage.category)

            val structured = run.messages.filterIsInstance<StructuredOutputMessageDto>().single()
            assertEquals(SenderDto.Model, structured.sender)
            assertEquals(MessageCategoryDto.EXECUTION, structured.category)
            assertTrue(structured.response.contains("INV-42"))
            assertTrue(run.messages.none { it is TextMessageDto && it.sender == SenderDto.Model })

            val result = run.result
            assertTrue(result is FinishedResultDto)
            assertTrue(result.response!!.contains("INV-42"))
        }
}
