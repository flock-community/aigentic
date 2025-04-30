# Aigentic Project Guidelines for Junie

## Project Overview
Aigentic is a Kotlin Multiplatform library that provides a powerful DSL for building and integrating AI agents into applications. It streamlines the process of creating, deploying, and managing LLM agents within the Aigentic ecosystem.

### Key Features
- Intuitive Kotlin DSL for agent creation and management
- LLM model agnostic (supports OpenAI, Gemini, Ollama, and more)

## Project Structure
The project is organized into several modules:

1. **Core Module** (`src/core/`): Contains the core interfaces and classes for the Aigentic library. Core doesn't depend on other modules:
   - Agent configuration and execution
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
2**Consider edge cases**: When implementing new features or fixing bugs, consider edge cases and ensure they are properly handled.

## Build Guidelines
- The project uses Gradle for building
- Before submitting changes, ensure the project is formatted correctly using the `spotlessApply` command
- Before submitting changes, ensure the project builds successfully using the `build` command
- Pay attention to any compiler warnings or errors

## Code Style Guidelines
- Follow Kotlin coding conventions
- Keep functions focused and concise
- Use meaningful variable and function names
- Don't use comments on variables and function names
- Use functional programming style
- Don't add debug logging or other println's
- Use the YAGNI principle
