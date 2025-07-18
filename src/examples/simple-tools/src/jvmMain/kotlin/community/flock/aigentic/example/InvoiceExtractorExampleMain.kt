package community.flock.aigentic.example

import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        invoiceExtractorAgent(
            FileReader.readFileBase64("/test-invoice.pdf"),
            geminiKey,
        )
    }
}
