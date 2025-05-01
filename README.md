[![stability-wip](https://img.shields.io/badge/stability-wip-lightgrey.svg)](https://github.com/mkenney/software-guides/blob/master/STABILITY-BADGES.md#work-in-progress)
![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/flock-community/aigentic/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/community.flock.aigentic/http-tools?color=blue&label=Download)](https://central.sonatype.com/namespace/community.flock.aigentic)
[![License](https://img.shields.io/github/license/flock-community/aigentic?color=yellow)](LICENSE)
[![Documentation](https://img.shields.io/badge/docs-api-a97bff.svg?logo=kotlin)](https://flock-community.github.io/aigentic/)

# AIGENTIC: Streamline Your LLM Development Journey

## Overview

The Aigentic Client Library is a powerful Kotlin-based DSL that enables developers to easily build and integrate AI agents into their applications. 
This library streamlines the process of creating, deploying, and managing LLM agents within the Aigentic ecosystem.

Features

- Intuitive Kotlin DSL for agent creation and management
- Easily connect agents to your existing applications
- LLM model agnostic
- Comprehensive error handling and logging
- Seamless integration with the Aigentic platform (coming soon)
- Built-in tools for rapid iteration and testing (coming soon)


## Aigentics vision

In AI and LLM application development, there's a paradoxical challenge: creating a promising proof of concept (PoC) is relatively easy, but transitioning that PoC into a robust, production-grade application is often complex and daunting. This gap between demonstration and deployment can lead to stalled projects and unrealized potential.
Aigentic directly addresses this challenge by providing a comprehensive platform designed to streamline the journey from PoC to production. Our toolset facilitates rapid iteration on your AI agents, allowing you to quickly refine and enhance your applications based on real-world data and feedback. By offering a shortened feedback loop, Aigentic enables developers to:

1. Develop and deploy agents integrated with your existing applications
2. Collect and analyse real-world usage data
3. Swiftly modify tasks and prompts
4. Test changes against historical data
5. Implement improvements with confidence

This iterative approach, supported by Aigentic, significantly reduces the time and complexity involved in developing production-ready AI applications. 
With Aigentic, the path from a promising demo to a reliable, scalable AI solution becomes clear and achievable, helping you unlock the full potential of LLM technology for your business.

```
    +--------------------------------------------------+
    |            Aigentic development cycle            |
    |                                                  |
    |   Develop         Deploy       Collect           |
    |   LLM Agent  -->  Agent   -->  Real-world Data   |
    |      ^                               |           |
    |      |                               |           |
    |      |                               v           |
    |  Implement      Replay         Modify Task/      |
    |  Changes    <-- Historical <--    Prompt         |
    |                 Data                             |
    |                                                  |
    +--------------------------------------------------+

```

# Getting Started

The Aigentic client library is a Kotlin Multiplatform library that can be used in JVM, Android, iOS, and JavaScript/TypeScript projects. Add the modules you need to your project:

```
implementation("community.flock.aigentic:core:<version>")

// Also available: community.flock.aigentic:gemini or community.flock.aigentic:ollama 
implementation("community.flock.aigentic:openai:<version>") 
 
// Add the ktor client library depending on your platform. CIO is for JVM, Android, Native. For other platforms pick the correct engine: https://ktor.io/docs/client-engines.html#platforms
implementation("io.ktor:ktor-client-cio:2.3.10")

```

With the dependencies added, you can start creating agents. Here is a simple example of an agent which has access to a tool that saves the sentiment of a news event:

```kotlin
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.tool.*
import community.flock.aigentic.openai.dsl.openAIModel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
import kotlinx.serialization.json.JsonObject


val agent = agent {
  task("Summarize the newsfeed of the day and determine the sentiment") {
    addInstruction("Analyze the sentiment for each news event")
    addInstruction("Save the sentiment for each news event")
  }
  openAIModel {
    apiKey("<insert your OpenAI API key here>")
    modelIdentifier(GPT4OMini)
  }
  addTool(saveNewsEventSentimentTool)
}

val saveNewsEventSentimentTool =
  object : Tool {

    override val name = ToolName("saveNewsEventSentiment")
    override val description = "Saves the news event components"

    val titleParameter = Parameter.Primitive(
      name = "title",
      description = "The title of the news event",
      isRequired = true,
      type = ParameterType.Primitive.String
    )

    val sentimentParameter =
      Parameter.Complex.Enum(
        "sentiment",
        "The sentiment of the news event",
        true,
        default = null,
        values = listOf(
          PrimitiveValue.String("positive"),
          PrimitiveValue.String("negative"),
          PrimitiveValue.String("neutral")
        ),
        valueType = ParameterType.Primitive.String,
      )


    override val parameters = listOf(titleParameter, sentimentParameter)

    override val handler: suspend (JsonObject) -> String = { arguments ->

      val title = titleParameter.getStringValue(arguments)
      val sentiment = sentimentParameter.getStringValue(arguments)

      "Saved successfully: '$title' with sentiment: $sentiment"
    }
  }

```

To start the agent call it's `start` function:

```kotlin
val inputData = listOf(addText(newsFeed))
val run = agent.start(inputData)
```

![Agent logging](./docs/images/agent-logging.gif)

## OpenAPI tools

Aigentic supports automatic tool configuration based on OpenAPI specifications. 

Add the OpenAPI module to your project:

```
implementation("community.flock.aigentic:openapi:<version>")
```

With the OpenAPI module added you can create an agent with OpenAPI tools, simple configure your OpenAPI specification and the agent will automatically generate the tools for you:


```kotlin
agent {
  task("Send Hacker News stories about AI") {
    addInstruction("Retrieve the top 10 Hacker News stories")
    addInstruction("Send stories, if any, about AI to john@doe.com")
  }
  openAIModel {
    apiKey("<insert your OpenAI API key here>")
    modelIdentifier(GPT4OMini)
  }
  openApiTools(hackerNewsOpenAPISpecJson)
  addTool(sendEmailTool)
}.start()
```

## Examples

For more detailed examples, please refer to:

- [Custom tool agent examples](src/examples/simple-tools/src/jvmMain/kotlin/community/flock/aigentic/example/RunExamples.kt)
- [OpenAPI tools agent example](src/examples/openapi-tools/src/commonMain/kotlin/community/flock/aigentic/example/OpenAPIAgentExample.kt)
- [GCP Cloud Function agent example](src/examples/google-http-cloud-function/src/jsMain/kotlin/community/flock/aigentic/cloud/google/httpcloudfunction/GoogleHttpCloudFunctionExample.kt)

# Aigentic platform

In addition to the client library, Aigentic provides a platform for managing and deploying agents. The platform is currently in development and will be available soon.

## Validate your agents task

In order to validate your agent's task and to quantify its performance you can use the `regressionTest` DSL. This DSL allows you to run historical runs against your agent and compare the results.
This enables you to test the following aspects of your agent:

- Stability: Does the agent produce consistent results over time?
- Regression: Does the agent's performance degrade after I make changes?
- Improvement: Does the agent's performance improve after I make changes?


```kotlin

val testReport = regressionTest {
    // The agent which will be tested, you can make changes to this agent and test the performance
    agent(licencePlateExtractor)
    // Historical tagged runs from Aigentic platform which will be used to test the agent
    addTag("validated")
    // The number each run should be repeated
    numberOfIterations(5)
}.start()

testReport.prettyPrint()

```



# How to use snapshots

In order to use SNAPSHOT versions of Aigentic please make sure both maven central and the Sonatype snapshot repository are configured:

```
repositories {
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
}
```

## License

Aigentic is released under the MIT License.

## Support

For questions, issues, or feature requests, please open an issue on our GitHub repository. Or contact us as [info@aigentic.io](mailto:info@aigentic.io?subject=Aigentic)
