export const openApiExample = `val orderAgent = agent<String, Answer> {
    geminiModel {
        apiKey("YOUR_API_KEY")
        modelIdentifier(GeminiModelIdentifier.Gemini3_5Flash)
    }
    task("Answer questions about customer orders") {
        addInstruction("Look up the order before you answer")
    }
    openApiTools(File("orders-api.json").readText()) {
        addHeader(Header.Bearer("YOUR_API_TOKEN"))
    }
}

val run = orderAgent.start("Where is order 1042?")`;

export const invoiceExtractorExample = `@AigenticParameter
data class InvoiceLine(val description: String, val total: Double)

@AigenticParameter
data class Invoice(
    val invoiceNumber: String,
    val lines: List<InvoiceLine>,
    @Description("Total amount before tax")
    val totalAmount: Double,
    val tax: Double,
)

val invoiceExtractor = agent<Unit, Invoice> {
    geminiModel {
        apiKey("YOUR_API_KEY")
        modelIdentifier(GeminiModelIdentifier.Gemini3_5Flash)
    }
    task("Extract the invoice from the attached PDF") {
        addInstruction("Include every line item")
    }
}

val run = invoiceExtractor.start(Attachment.Base64.pdf(invoicePdf))
val invoice = (run.outcome as? Outcome.Finished)?.response`;

export const workflowExample = `@AigenticParameter
data class Document(val content: String)

@AigenticParameter
data class Analysis(val topics: List<String>, val tone: String)

@AigenticParameter
data class Summary(val text: String)

val analyzer = agent<Document, Analysis> {
    geminiModel {
        apiKey("YOUR_API_KEY")
        modelIdentifier(GeminiModelIdentifier.Gemini3_5Flash)
    }
    task("Find the key topics and the overall tone") {}
}

val summarizer = agent<Analysis, Summary> {
    openAIModel {
        apiKey("YOUR_API_KEY")
        modelIdentifier(OpenAIModelIdentifier.GPT5_5)
    }
    task("Summarize the analysis in under 100 words") {}
}

val workflow = analyzer thenProcess summarizer
val run = workflow.start(Document("Raw document text..."))`;

export const goldenSetExample = `val invoiceExtractor = agent<Unit, Invoice> {
    // model and task as above
    platform {
        name("invoice-extractor")
        secret("YOUR_PLATFORM_SECRET")
    }
}

// Real invoices, with the answers you already know
goldenSet.forEach { (pdf, expected) ->
    invoiceExtractor.start(
        null,
        Attachment.Base64.pdf(pdf),
        expected = Expected("invoices-golden-set", expected),
    )
}`;
