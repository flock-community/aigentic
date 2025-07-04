package community.flock.aigentic.example

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.annotations.AigenticResponse
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier

@AigenticParameter
data class InvoiceComponents(
    val components: List<InvoiceComponent>,
)

@AigenticParameter
data class InvoiceComponent(
    val name: String,
    val value: String,
)

@AigenticResponse
data class SaveResult(val message: String)

suspend fun invoiceExtractorAgent(
    invoicePdfBase64: String,
    apiKey: String,
) {
    val run =
        agent {
            geminiModel {
                apiKey(apiKey)
                modelIdentifier(GeminiModelIdentifier.Gemini2_0Flash)
            }
            task("Extract the different invoice components") {
                addInstruction("Please provide list of the invoice components")
            }
            addTool("saveInvoiceComponents") { input: InvoiceComponents ->
                SaveResult("Saved ${input.components.size} invoice components successfully")
            }
            context {
                addBase64(invoicePdfBase64, MimeType.PDF)
            }
        }.start()

    when (val result = run.result) {
        is community.flock.aigentic.core.agent.tool.Result.Finished -> "Agent finished successfully"
        is community.flock.aigentic.core.agent.tool.Result.Stuck -> "Agent is stuck and could not complete task, it says: ${result.reason}"
        is community.flock.aigentic.core.agent.tool.Result.Fatal -> "Agent crashed: ${result.message}"
    }.also(::println)
}
