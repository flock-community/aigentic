# Aigentic DSL Usage Guide

## Table of Contents
- [Introduction](#introduction)
- [Getting Started](#getting-started)
  - [Installation](#installation)
  - [Basic Concepts](#basic-concepts)
- [Creating and Configuring Agents](#creating-and-configuring-agents)
  - [Agent Configuration](#agent-configuration)
  - [Task Configuration](#task-configuration)
  - [Input Data Configuration](#input-data-configuration)
- [Using Different LLM Providers](#using-different-llm-providers)
  - [OpenAI](#openai)
  - [Gemini](#gemini)
  - [Ollama](#ollama)
- [Working with Tools](#working-with-tools)
  - [Creating Custom Tools](#creating-custom-tools)
  - [Tool Parameters](#tool-parameters)
  - [Using OpenAPI Tools](#using-openapi-tools)
  - [Using HTTP Tools](#using-http-tools)
- [Advanced Usage](#advanced-usage)
  - [Structured Responses](#structured-responses)
  - [Error Handling](#error-handling)
  - [Regression Testing](#regression-testing)
- [Examples](#examples)
  - [Simple Agent with Custom Tools](#simple-agent-with-custom-tools)
  - [Agent with OpenAPI Tools](#agent-with-openapi-tools)
  - [Agent with Structured Response](#agent-with-structured-response)

## Introduction

Aigentic is a powerful Kotlin Multiplatform library that provides a Domain-Specific Language (DSL) for building and integrating AI agents into your applications. This guide will walk you through how to use the Aigentic DSL to create, configure, and deploy agents that can perform a wide variety of tasks.

The Aigentic DSL is designed to be intuitive and flexible, allowing you to:
- Create agents with specific tasks and instructions
- Configure different LLM providers (OpenAI, Gemini, Ollama)
- Add custom tools for agents to use
- Provide input data for agents to work with
- Define structured response formats
- Handle errors and test your agents

## Getting Started

### Installation

To use Aigentic in your project, add the required dependencies to your build file:

```kotlin
// Add the core module
implementation("community.flock.aigentic:core:<version>")

// Add the provider module for your preferred LLM
implementation("community.flock.aigentic:openai:<version>") 
// Also available: community.flock.aigentic:gemini or community.flock.aigentic:ollama

// Add the Ktor client library for your platform
implementation("io.ktor:ktor-client-cio:2.3.10") // For JVM, Android, Native
```

### Basic Concepts

The Aigentic DSL revolves around a few key concepts:

1. **Agent**: The central entity that performs tasks using LLM capabilities
2. **Task**: What the agent needs to accomplish, with specific instructions
3. **Model**: The LLM model that powers the agent (OpenAI, Gemini, etc.)
4. **Tools**: Functions that the agent can use to perform actions
5. **Input Data**: The input that changes each run

## Creating and Configuring Agents

The entry point for creating an agent is the `agent` function, which takes a configuration block:

```kotlin
val agent = agent {
    // Configure the agent here
}
```

### Agent Configuration

An agent requires at minimum:
- A task to perform
- A model to use
- At least one tool or a response parameter

Here's a basic agent configuration:

```kotlin
val agent = agent {
    task("Summarize the text") {
        addInstruction("Create a concise summary")
        addInstruction("Highlight key points")
    }

    openAIModel {
        apiKey("<your-api-key>")
        modelIdentifier(OpenAIModelIdentifier.GPT4OMini)
    }

    addTool(summarizeTool)
}
```

### Task Configuration

Tasks define what the agent needs to accomplish. A task has a description and can have multiple instructions:

```kotlin
task("Analyze customer feedback") {
    addInstruction("Identify common themes")
    addInstruction("Categorize feedback as positive, negative, or neutral")
    addInstruction("Suggest actionable improvements")
}
```

### Input Data Configuration

You can provide additional input data to the agent when starting it:

```kotlin
val inputData = listOf(
    InputData.Text("Here is some background information..."),
    InputData.Base64(encodedImage, MimeType.ImagePng)
)

val run = agent.start(inputData)
```

## Using Different LLM Providers

Aigentic supports multiple LLM providers, each with its own configuration.

### OpenAI

```kotlin
openAIModel {
    apiKey("<your-openai-api-key>")
    modelIdentifier(OpenAIModelIdentifier.GPT4OMini)

    // Optional: Configure generation settings
    generationConfig {
        temperature(0.7f)
        topP(0.9f)
    }
}
```

Available OpenAI models include:
- GPT4O
- GPT4OMini
- GPT4Turbo
- GPT3_5Turbo
- GPT4_1
- GPT4_1Mini
- GPT4_1Nano
- GPT4_5Preview
- O1
- O1Pro
- O1Mini
- O3
- O3Mini
- O4Mini
- GPT4OMiniSearchPreview
- GPT4OSearchPreview
- Custom (for any other model identifier)

### Gemini

```kotlin
geminiModel {
    apiKey("<your-gemini-api-key>")
    modelIdentifier(GeminiModelIdentifier.Gemini2_5FlashPreview)

    // Optional: Configure generation settings
    generationConfig {
        temperature(0.7f)
        topP(0.9f)
    }
}
```

Available Gemini models include:
- Gemini2_5FlashPreview
- Gemini2_5ProPreview
- Gemini2_0Flash
- Gemini2_0FlashLite
- Gemini1_5Flash
- Gemini1_5Flash8b
- Gemini1_5Pro
- Custom (for any other model identifier)

### Ollama

```kotlin
ollamaModel {
    modelIdentifier(object : ModelIdentifier {
        override val stringValue: String = "llama3.1"
    })

    // Optional: Configure generation settings
    generationConfig {
        temperature(0.7f)
        topP(0.9f)
    }
}
```

## Working with Tools

Tools are functions that agents can use to perform actions. Aigentic provides several ways to create and use tools.

### Creating Custom Tools

You can create custom tools by implementing the `Tool` interface:

```kotlin
val weatherTool = object : Tool {
    val cityParameter = Parameter.Primitive(
        name = "city",
        description = "The city to get weather for",
        isRequired = true,
        type = ParameterType.Primitive.String
    )

    override val name = ToolName("getWeather")
    override val description = "Gets the current weather for a city"
    override val parameters = listOf(cityParameter)

    override val handler: suspend (JsonObject) -> String = { arguments ->
        val city = cityParameter.getStringValue(arguments)
        // Fetch and return weather data
        "The weather in $city is sunny with a high of 75°F"
    }
}
```

### Tool Parameters

Tools can have parameters of different types:

1. **Primitive Parameters**:
   - String
   - Number
   - Integer
   - Boolean

```kotlin
Parameter.Primitive(
    name = "query",
    description = "Search query",
    isRequired = true,
    type = ParameterType.Primitive.String
)
```

2. **Complex Parameters**:
   - Enum
   - Object
   - Array

```kotlin
// Enum parameter
Parameter.Complex.Enum(
    name = "sortOrder",
    description = "Sort order for results",
    isRequired = true,
    default = PrimitiveValue.String("ascending"),
    values = listOf(
        PrimitiveValue.String("ascending"),
        PrimitiveValue.String("descending")
    ),
    valueType = ParameterType.Primitive.String
)

// Object parameter
Parameter.Complex.Object(
    name = "filter",
    description = "Filter criteria",
    isRequired = false,
    parameters = listOf(
        Parameter.Primitive("minPrice", "Minimum price", false, ParameterType.Primitive.Number),
        Parameter.Primitive("maxPrice", "Maximum price", false, ParameterType.Primitive.Number)
    )
)

// Array parameter
Parameter.Complex.Array(
    name = "tags",
    description = "List of tags",
    isRequired = false,
    itemDefinition = Parameter.Primitive("tag", "A tag", true, ParameterType.Primitive.String)
)
```

### Using OpenAPI Tools

Aigentic can automatically generate tools from OpenAPI specifications:

```kotlin
// Add the OpenAPI module
implementation("community.flock.aigentic:openapi:<version>")

// Use OpenAPI tools in your agent
agent {
    task("Fetch and analyze data") {
        addInstruction("Retrieve data from the API")
        addInstruction("Analyze the results")
    }

    openAIModel {
        apiKey("<your-api-key>")
        modelIdentifier(OpenAIModelIdentifier.GPT4OMini)
    }

    // Add tools from an OpenAPI specification
    openApiTools(apiSpecificationJson)
}
```

### Using HTTP Tools

Aigentic provides tools for making HTTP requests:

```kotlin
// Add the HTTP tools module
implementation("community.flock.aigentic:http-tools:<version>")

// Use HTTP tools in your agent
// (See HTTP tools documentation for details)
```

## Advanced Usage

### Structured Responses

You can define structured response formats for your agents:

```kotlin
// Define a response class
@Serializable
data class WeatherResponse(
    val temperature: Float,
    val conditions: String,
    val forecast: List<String>
)

// Create a parameter for the response
val weatherResponseParameter = Parameter.Complex.Object(
    name = "response",
    description = "Weather information",
    isRequired = true,
    parameters = listOf(
        Parameter.Primitive("temperature", "Current temperature in °F", true, ParameterType.Primitive.Number),
        Parameter.Primitive("conditions", "Current weather conditions", true, ParameterType.Primitive.String),
        Parameter.Complex.Array(
            name = "forecast",
            description = "Weather forecast for the next 3 days",
            isRequired = true,
            itemDefinition = Parameter.Primitive("day", "Forecast for a day", true, ParameterType.Primitive.String)
        )
    )
)

// Configure the agent to use the response parameter
agent {
    // ... other configuration
    finishResponse(weatherResponseParameter)
}

// Get the structured response
val run = agent.start()
when (val result = run.result) {
    is Result.Finished -> {
        val response = result.getFinishResponse<WeatherResponse>()
        println("Temperature: ${response.temperature}°F")
        println("Conditions: ${response.conditions}")
        println("Forecast:")
        response.forecast.forEach { println("- $it") }
    }
    // Handle other results
}
```

### Error Handling

Aigentic provides error handling mechanisms for agent runs:

```kotlin
val run = agent.start()
when (val result = run.result) {
    is Result.Finished -> {
        // Agent completed successfully
        println("Success: ${result.response}")
    }
    is Result.Stuck -> {
        // Agent got stuck and couldn't complete the task
        println("Agent is stuck: ${result.reason}")
    }
    is Result.Fatal -> {
        // A fatal error occurred
        println("Fatal error: ${result.message}")
    }
}
```

### Regression Testing

Aigentic provides a regression testing framework to validate your agents:

```kotlin
val testReport = regressionTest {
    // The agent to test
    agent(myAgent)

    // Historical tagged runs to test against
    addTag("validated")

    // Number of iterations for each run
    numberOfIterations(5)
}.start()

// Print the test report
testReport.prettyPrint()
```

## Examples

### Simple Agent with Custom Tools

Here's a complete example of an agent that uses custom tools:

```kotlin
val calculateTool = object : Tool {
    val expressionParameter = Parameter.Primitive(
        name = "expression",
        description = "The mathematical expression to calculate",
        isRequired = true,
        type = ParameterType.Primitive.String
    )

    override val name = ToolName("calculate")
    override val description = "Calculates the result of a mathematical expression"
    override val parameters = listOf(expressionParameter)

    override val handler: suspend (JsonObject) -> String = { arguments ->
        val expression = expressionParameter.getStringValue(arguments)
        // Simple expression evaluator (for demonstration)
        try {
            val result = eval(expression)
            "The result of $expression is $result"
        } catch (e: Exception) {
            "Error calculating $expression: ${e.message}"
        }
    }
}

val agent = agent {
    task("Solve mathematical problems") {
        addInstruction("Solve the given mathematical problems")
        addInstruction("Show your work")
    }


    openAIModel {
        apiKey("<your-api-key>")
        modelIdentifier(OpenAIModelIdentifier.GPT4OMini)
    }

    addTool(calculateTool)
}

val inputData = listOf(InputData.Text("Please solve: 1. 25 * 4 + 10, 2. (17 + 8) / 5"))
val run = agent.start(inputData)

when (val result = run.result) {
    is Result.Finished -> println("Result: ${result.response}")
    is Result.Stuck -> println("Agent is stuck: ${result.reason}")
    is Result.Fatal -> println("Fatal error: ${result.message}")
}
```

### Agent with OpenAPI Tools

Here's an example of an agent that uses tools generated from an OpenAPI specification:

```kotlin
val agent = agent {
    task("Get information about Hacker News stories") {
        addInstruction("Retrieve the top 10 Hacker News stories")
        addInstruction("Summarize the stories about AI or machine learning")
    }

    openAIModel {
        apiKey("<your-api-key>")
        modelIdentifier(OpenAIModelIdentifier.GPT4OMini)
    }

    // Add tools from the Hacker News API specification
    openApiTools(hackerNewsApiSpec)
}

val run = agent.start()
// Handle the result
```

### Agent with Structured Response

Here's an example of an agent that returns a structured response:

```kotlin
@Serializable
data class NewsAnalysis(
    val totalStories: Int,
    val aiRelatedStories: List<String>,
    val topTrends: List<String>
)

val newsAnalysisParameter = Parameter.Complex.Object(
    name = "analysis",
    description = "Analysis of Hacker News stories",
    isRequired = true,
    parameters = listOf(
        Parameter.Primitive("totalStories", "Total number of stories analyzed", true, ParameterType.Primitive.Integer),
        Parameter.Complex.Array(
            name = "aiRelatedStories",
            description = "Titles of AI-related stories",
            isRequired = true,
            itemDefinition = Parameter.Primitive("title", "Story title", true, ParameterType.Primitive.String)
        ),
        Parameter.Complex.Array(
            name = "topTrends",
            description = "Top trends identified in the stories",
            isRequired = true,
            itemDefinition = Parameter.Primitive("trend", "Trend description", true, ParameterType.Primitive.String)
        )
    )
)

val agent = agent {
    task("Analyze Hacker News stories") {
        addInstruction("Retrieve and analyze the top Hacker News stories")
        addInstruction("Identify AI-related stories and emerging trends")
    }

    openAIModel {
        apiKey("<your-api-key>")
        modelIdentifier(OpenAIModelIdentifier.GPT4OMini)
    }

    openApiTools(hackerNewsApiSpec)
    finishResponse(newsAnalysisParameter)
}

val run = agent.start()
when (val result = run.result) {
    is Result.Finished -> {
        val analysis = result.getFinishResponse<NewsAnalysis>()
        println("Analyzed ${analysis.totalStories} stories")
        println("AI-related stories:")
        analysis.aiRelatedStories.forEach { println("- $it") }
        println("Top trends:")
        analysis.topTrends.forEach { println("- $it") }
    }
    // Handle other results
}
```
