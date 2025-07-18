package community.flock.aigentic.example

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.annotations.AigenticParameter
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

@AigenticParameter
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

    when (val result = run.outcome) {
        is Outcome.Finished -> "Agent finished successfully"
        is Outcome.Stuck -> "Agent is stuck and could not complete task, it says: ${result.reason}"
        is Outcome.Fatal -> "Agent crashed: ${result.message}"
    }.also(::println)
}
