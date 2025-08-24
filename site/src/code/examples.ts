export const weatherAgent = `
// Describe the agents response type
@AigenticParameter
data class WeatherResponse(
    @Description("Current temperature in degrees Celsius")
    val temperature: Double,
    @Description("Current weather conditions description")
    val conditions: String,
    @Description("Name of the location for the weather data")
    val location: String
)

// Describe the getWeather tool request type
@AigenticParameter
data class WeatherRequest(
    @Description("Name of the location to get weather information for")
    val location: String
)

// Configure the agent
val weatherAgent =
        agent<String, WeatherResponse> {
            // Configure the model for the agent, other models are also available
            geminiModel {
                apiKey("YOUR_API_KEY")
                modelIdentifier(GeminiModelIdentifier.Gemini2_5Flash)
            }

            // Configure the task for the agent
            task("Provide weather information") {
                addInstruction("Respond to user queries about weather")
            }

            // Add a weather tool to give the agent live weather information capabilities
            addTool("getWeather", "Get the current weather for a location") { req: WeatherRequest ->
                WeatherClient.requestWeather(req.location)
            }
        }

// Start the agent and get a run
val run = weatherAgent.start("What's the weather like in Amsterdam?")

// Print the result
when (val outcome = run.outcome) {
  is Outcome.Finished -> println("Weather in \${outcome.response?.location}: \${outcome.response?.temperature}°C, \${outcome.response?.conditions}")
  is Outcome.Stuck -> println("Agent is stuck: \${outcome.reason}")
  is Outcome.Fatal -> println("Error: \${outcome.message}")
}

// Print token usage summary to monitor resource consumption
println(run.getTokenUsageSummary())
`

export const invoiceExtractorExample = `
@AigenticParameter
data class InvoiceLine(
    val description: String,
    val quantity: Int,
    val lineTotal: Double
)

@AigenticParameter
data class InvoiceComponents(
    val invoiceNumber: String,
    val lines: List<InvoiceLine>,
    @Description("Total amount before tax")
    val totalAmount: Double,
    @Description("Tax amount applied to the invoice")
    val tax: Double
)

@AigenticParameter
data class SaveResult(
    val message: String
)

// Configure the agent
val invoiceExtractorAgent =
        agent<Base64Image, InvoiceComponents> {
            // Configure the model for the agent, other models are also available
            geminiModel {
                apiKey("YOUR_API_KEY")
                modelIdentifier(GeminiModelIdentifier.Gemini2_0Flash)
            }

            // Configure the task for the agent
            task("Extract structured invoice data from images") {
                addInstruction("Extract invoice number, line items, amounts, and tax information")
                addInstruction("Save each extracted invoice")
            }

            // Add a tool to save invoice data
            addTool("saveInvoiceData", "Save extracted invoice data to system") { input: InvoiceComponents ->
                SaveResult(message = "Saved invoice \${input.invoiceNumber} with \${input.lines.size} line items successfully")
            }
        }

// Start the agent and get a run
val run = invoiceExtractorAgent.start("base64Invoice")

// Print the result
when (val outcome = run.outcome) {
    is Outcome.Finished -> println("Extracted invoice \${outcome.response")
    is Outcome.Stuck -> println("Agent is stuck: \${outcome.reason}")
    is Outcome.Fatal -> println("Error: \${outcome.message}")
}

// Print token usage summary to monitor resource consumption
println(run.getTokenUsageSummary())
`

export const workflowExample = `
@AigenticParameter
data class Document(val content: String)

@AigenticParameter
data class ProcessedDoc(val cleanedContent: String)

@AigenticParameter
data class AnalyzedDoc(
    val content: String,
    val keyTopics: List<String>,
    val sentiment: String
)

@AigenticParameter
data class Summary(val summary: String)

// Create specialized agents
val cleaner = agent<Document, ProcessedDoc> {
    geminiModel {
        apiKey("YOUR_API_KEY")
        modelIdentifier(GeminiModelIdentifier.Gemini2_5Flash)
    }
    task("Clean and format document") {
        addInstruction("Remove formatting artifacts")
        addInstruction("Fix grammar and spelling")
    }
}

val analyzer = agent<ProcessedDoc, AnalyzedDoc> {
    geminiModel {
        apiKey("YOUR_API_KEY")
        modelIdentifier(GeminiModelIdentifier.Gemini2_5Flash)
    }
    task("Analyze document content") {
        addInstruction("Identify key topics and themes")
        addInstruction("Determine overall sentiment")
    }
}

val summarizer = agent<AnalyzedDoc, Summary> {
    geminiModel {
        apiKey("YOUR_API_KEY")
        modelIdentifier(GeminiModelIdentifier.Gemini2_5Flash)
    }
    task("Create concise summary") {
        addInstruction("Highlight key points and topics")
        addInstruction("Keep under 100 words")
    }
}

// Chain agents into workflow
val workflow = cleaner thenProcess analyzer thenProcess summarizer

// Execute workflow
val run = workflow.start(Document("Raw document text..."))

when (val outcome = run.outcome) {
    is Outcome.Finished -> println(outcome.response?.summary)
    is Outcome.Stuck -> println("Workflow stuck: \${outcome.reason}")
    is Outcome.Fatal -> println("Error: \${outcome.message}")
}`
