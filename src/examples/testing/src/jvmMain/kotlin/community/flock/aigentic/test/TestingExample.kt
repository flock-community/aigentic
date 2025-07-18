package community.flock.aigentic.test

import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.annotations.Description
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.tool.createTool
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import community.flock.aigentic.platform.dsl.platform
import community.flock.aigentic.platform.dsl.regressionTest
import community.flock.aigentic.platform.testing.start
import kotlinx.coroutines.runBlocking

@AigenticParameter
data class InvoiceExtractInput(
    val invoiceNumber: String,
    val debtorNumber: String,
    val invoiceTotal: String,
    val licencePlates: List<String>,
)

val invoiceExtractTool =
    createTool<InvoiceExtractInput, String>(
        name = "saveInvoiceTool",
        description = "Saves the invoice details",
    ) {
        error("I shouldn't be called")
    }

private val geminiKey by lazy {
    System.getenv("GEMINI_API_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'GEMINI_API_KEY' environment variable!")
    }
}

@AigenticParameter
data class Explanation(
    @Description("Please explain the provided document as accurate as possible")
    val explanation: String,
)

val licencePlateExtractor =
    agent<Unit, Explanation> {
        task("Extract the invoice elements from the provided document") {}
        addTool(invoiceExtractTool)
        geminiModel {
            apiKey(geminiKey)
            modelIdentifier(GeminiModelIdentifier.Gemini2_0Flash)
        }
        platform {
            name("licence-plate-extractor")
            secret("<platform-agent-api-key>")
        }
    }

fun main(): Unit =
    runBlocking {
        regressionTest {
            agent(licencePlateExtractor)
            tags("validated")
            numberOfIterations(2)
        }.start()
    }
