# Aigentic Project Guidelines for Junie

## Project Overview
Aigentic is a Kotlin Multiplatform library that provides a powerful DSL for building and integrating AI agents into applications. It streamlines the process of creating, deploying, and managing LLM agents within the Aigentic ecosystem.

### Key Features
- Intuitive Kotlin DSL for agent creation and management
- LLM model agnostic (supports OpenAI, Gemini, Ollama, and more)

## DSL Usage
Aigentic provides a powerful and intuitive DSL for creating and configuring AI agents. Here's an example of how to use it:

It provides 2 main dsl:

- Agent DSL: dsl to compose an agent
- Workflow DSL dsl to chain multiple agents into a workflow

Required imports:
- community.flock.aigentic.core.agent.start
- community.flock.aigentic.core.agent.tool.Outcome
- community.flock.aigentic.core.annotations.AigenticParameter
- community.flock.aigentic.core.dsl.agent
- community.flock.aigentic.openai.dsl.openAIModel
- community.flock.aigentic.openai.model.OpenAIModelIdentifier

```kotlin
suspend fun main() {
    @AigenticParameter
    data class Answer(
        val answer: String,
    )

    // Define a question answering agent using the DSL.
    // agent<I, O> where:
    // - I (String): Input type the agent accepts in its start function (user questions as strings)
    // - O (Answer): Output type the agent produces in its Finished Outcome (answer data)
    val questionAgent =
        agent<String, Answer> {
            // Configure the model for the agent, other models are also available
            openAIModel {
                apiKey("YOUR_API_KEY")
                modelIdentifier(OpenAIModelIdentifier.GPT4O)
            }

            // Configure the task for the agent
            // This defines the agent's purpose and provides instructions for its behavior
            task("Answer questions about Kotlin Multiplatform") {
                addInstruction("Provide concise and accurate answers")
            }
        }

    // The start function expects the I type from the agent: agent<String, Answer>
    val run = questionAgent.start("What is cool about kotlin?")

    // Evaluate the Runs outcome
    when (val outcome = run.outcome) {
        // Finished: Contains the successful response from the agent
        // It yields the O type from the agent: agent<String, Answer>
        // Access the response property to get the answer
        is Outcome.Finished -> println(outcome.response?.answer)

        // Stuck: Indicates the agent couldn't proceed with execution
        // Access the reason property to understand why the agent got stuck
        is Outcome.Stuck -> println("Agent is stuck: ${outcome.reason}")

        // Fatal: Represents a critical error during execution
        // Access the message property to get the error details
        is Outcome.Fatal -> println("Error: ${outcome.message}")
    }

    // Print token usage summary to monitor resource consumption
    println(run.getTokenUsageSummary())
}
```

This example demonstrates:
1. Creating an agent with specified input and output types
2. Configuring the agent with an OpenAI model
3. Defining the agent's task and instructions
4. Starting the agent with an input question
5. Handling different outcome types (Finished, Stuck, Fatal)
6. Retrieving token usage information

## Project Structure
The project is organized into several modules:

1. **Core Module** (`src/core/`): Contains the core interfaces and classes for the Aigentic library. Core doesn't depend on other modules:
   - Agent configuration and execution
   - Workflow configuration and execution
   - Model interfaces and abstractions
   - Tool definitions and implementations
   - Message handling

2. **Provider Modules** (`src/providers/`): Implementations for different LLM providers:
   - OpenAI (`src/providers/openai/`)
   - Gemini (`src/providers/gemini/`)
   - Ollama (`src/providers/ollama/`)
   - JSON Schema (`src/providers/jsonschema/`)

3. **Tools Module** (`src/tools/`): Common tools that can be used with agents:
   - HTTP tools (`src/tools/http/`)
   - OpenAPI tools (`src/tools/openapi/`)

4. **Examples** (`src/examples/`): Example implementations showing how to use Aigentic:
   - Simple tools examples
   - OpenAPI tools examples
   - Google Cloud Function examples
   - Testing examples

5. **Platform Module** (`src/platform/`): Platform-specific implementations and utilities

## Testing Guidelines
When working with this project, Junie should:

1. **Run tests for modified components**: After making changes to any file, run the relevant tests to ensure functionality is preserved.
2. **Consider edge cases**: When implementing new features or fixing bugs, consider edge cases and ensure they are properly handled.

## Build Guidelines
- The project uses Gradle for building
- Before submitting changes, ensure the project is formatted correctly using the `spotlessApply` command
- Before submitting changes, ensure the project builds successfully using the `build` command
- Pay attention to any compiler warnings or errors

## Code Style Guidelines
- Follow Kotlin coding conventions
- Keep functions focused and concise
- Use meaningful variable and function names
- Don't use comments on variables, function names or tests
- Use functional programming style
- Don't add debug logging or other println's
- Use the YAGNI principle

## Documentation Tone of Voice Guidelines
The Aigentic documentation website in `/site` follows specific tone guidelines to ensure content is accessible and user-friendly.

### Key Characteristics

#### Conversational and Natural
- Use everyday language rather than formal or academic phrasing
- Write as if you're explaining concepts to a colleague
- Avoid LLM-generated-sounding phrases like "crucial for effective" or "vital for optimal performance"

#### Direct and Clear
- Get to the point without unnecessary embellishment
- Use simple, active constructions: "This helps..." rather than "This is designed to facilitate..."
- Choose straightforward words over complex alternatives

#### Practical and User-Focused
- Focus on how features benefit the user
- Emphasize practical outcomes rather than theoretical advantages
- Frame explanations in terms of what users can accomplish

#### Approachable but Professional
- Maintain technical accuracy while being accessible
- Use a friendly tone without being overly casual
- Avoid jargon when possible, but don't oversimplify technical concepts

### Word Choice Examples

| Instead of (Too Formal) | Use (Conversational) |
|-------------------------|----------------------|
| "crucial for effective" | "significantly improves" |
| "vital for optimal" | "greatly enhances" |
| "enables the construction of" | "helps create" |
| "provides detailed context about" | "gives details about" |
| "facilitates improved performance" | "makes your tools work better" |
| "leads to enhanced outcomes" | "helps your agents work more effectively" |

### Sentence Structure
- Prefer shorter sentences with a clear subject-verb structure
- Use bullet points for lists rather than complex, comma-separated sentences
- Start with the benefit or main point, then explain details

### Examples

**Too formal/LLM-like:**
"The description property in @AigenticParameter is crucial for effective LLM interactions, providing essential context that enables the model to make optimal decisions regarding tool utilization."

**Conversational/Natural:**
"The description property in @AigenticParameter helps improves LLM interactions. This description gives the LLM context about how and when to use your tools."
