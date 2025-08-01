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
