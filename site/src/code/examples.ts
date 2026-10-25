export const providerExamples = [
  {
    label: "OpenAI",
    code: `agent<String, Answer> {
    openAIModel {
        apiKey("YOUR_API_KEY")
        modelIdentifier(OpenAIModelIdentifier.GPT5_5)
    }
    task("Answer questions about Kotlin") {}
}`,
  },
  {
    label: "Gemini",
    code: `agent<String, Answer> {
    geminiModel {
        apiKey("YOUR_API_KEY")
        modelIdentifier(GeminiModelIdentifier.Gemini3_8Flash)
    }
    task("Answer questions about Kotlin") {}
}`,
  },
  {
    label: "Ollama",
    code: `agent<String, Answer> {
    ollamaModel {
        apiUrl("http://localhost:11434/v1/")
        modelIdentifier(OpenAIModelIdentifier.Custom("llama3"))
    }
    task("Answer questions about Kotlin") {}
}`,
  },
  {
    label: "VertexAI",
    code: `agent<String, Answer> {
    vertexAIModel {
        project("YOUR_PROJECT_ID")
        location("europe-west4")
        modelIdentifier(VertexAIModelIdentifier.Gemini3_8Flash)
    }
    task("Answer questions about Kotlin") {}
}`,
  },
];

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
        modelIdentifier(GeminiModelIdentifier.Gemini3_8Flash)
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
        modelIdentifier(GeminiModelIdentifier.Gemini3_8Flash)
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

export const regressionTestExample = `val weatherAgent = agent<String, Weather> {
    // model, task and tools as above
    platform {
        name("weather-agent")
        secret("YOUR_PLATFORM_SECRET")
    }
}

val report = regressionTest {
    agent(weatherAgent)
    tags("validated")
    numberOfIterations(3)
}.start()

check(report.failures.isEmpty() && report.errors.isEmpty())`;
