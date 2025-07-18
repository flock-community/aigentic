package community.flock.aigentic.example

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.vertexai.VertexAIModelIdentifier
import community.flock.aigentic.vertexai.dsl.vertexAIModel
import kotlinx.coroutines.runBlocking

val vertexAIProject: String by lazy {
    System.getenv("VERTEX_AI_PROJECT").also {
        if (it.isNullOrEmpty()) error("Set 'VERTEX_AI_PROJECT' environment variable!")
    }
}

val vertexAILocation: String by lazy {
    System.getenv("VERTEX_AI_LOCATION").also {
        if (it.isNullOrEmpty()) error("Set 'VERTEX_AI_LOCATION' environment variable!")
    }
}

fun main() {
    runBlocking {
        runVertexAIWeatherExample(
            project = vertexAIProject,
            location = vertexAILocation,
        )
    }
}

/**
 * VertexAI is currently only available on JVM
 */
suspend fun runVertexAIWeatherExample(
    project: String,
    location: String,
) {
    val run =
        agent {
            vertexAIModel {
                project(project)
                location(location)
                modelIdentifier(VertexAIModelIdentifier.Gemini2_0Flash)
            }
            task("Provide weather information to users") {
                addInstruction("You are a helpful weather assistant that provides weather information for different locations.")
                addInstruction("Use the getWeather tool to fetch weather information when a user asks about the weather.")
                addInstruction("Get the weather for Utrecht, Netherlands")
            }
            addTool("getWeather") { input: WeatherRequest ->
                // This is a simple mock implementation of a weather tool
                // In a real application, this would call a weather API
                println("getWeather tool: Requesting weather for ${input.location} on ${input.date}")
                WeatherResponse(
                    temperature = "22°C",
                    conditions = "Partly cloudy with a slight chance of rain",
                    location = input.location,
                    date = input.date ?: "today",
                )
            }
        }.start()

    when (val result = run.outcome) {
        is community.flock.aigentic.core.agent.tool.Outcome.Finished -> "Agent finished successfully"
        is community.flock.aigentic.core.agent.tool.Outcome.Stuck -> "Agent is stuck and could not complete task, it says: ${result.reason}"
        is community.flock.aigentic.core.agent.tool.Outcome.Fatal -> "Agent crashed: ${result.message}"
    }.also(::println)
}
