[![stability-wip](https://img.shields.io/badge/stability-wip-lightgrey.svg)](https://github.com/mkenney/software-guides/blob/master/STABILITY-BADGES.md#work-in-progress)
![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/flock-community/aigentic/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/community.flock.aigentic/http-tools?color=blue&label=Download)](https://central.sonatype.com/namespace/community.flock.aigentic)
[![License](https://img.shields.io/github/license/flock-community/aigentic?color=yellow)](LICENSE)
[![Documentation](https://img.shields.io/badge/docs-aigentic.io-a97bff.svg?logo=kotlin)](https://aigentic.io)

# Aigentic: Streamline Your LLM Development Journey

Visit **[aigentic.io](https://aigentic.io)** for a complete overview.

Aigentic is a Kotlin Multiplatform library that provides a powerful DSL for building and integrating AI agents into applications. It streamlines the process of creating, deploying, and managing LLM agents within your software ecosystem.

## Features

- **Intuitive Kotlin DSL** for agent creation and management
- **Model Agnostic** - supports OpenAI, Gemini, Ollama, VertexAI, and more
- **Type-Safe Tools** with @AigenticParameter annotation
- **Comprehensive Integration** with OpenAPI specifications
- **Cross-Platform** - works on JVM, Android, iOS, and JavaScript


Create agents in a fully type-safe way:

```kotlin
@AigenticParameter
data class WeatherRequest(val location: String)

@AigenticParameter
data class WeatherResponse(
    val temperature: String,
    val conditions: String,
    val location: String
)

val agent = agent<String, WeatherResponse> {
    openAIModel {
        apiKey("YOUR_API_KEY")
        modelIdentifier(OpenAIModelIdentifier.GPT4O)
    }
    
    task("Provide weather information") {
        addInstruction("You are a helpful weather assistant")
        addInstruction("Use the getWeather tool to fetch current weather conditions")
    }
    
    // Add a weather lookup tool
    addTool("getWeather", "Get current weather for a location") { req: WeatherRequest ->
        WeatherResponse(
            temperature = "22°C",
            conditions = "Partly cloudy",
            location = req.location
        )
    }
}

val run = agent.start("What's the weather like in Amsterdam?")
```

## Documentation

For complete documentation, examples, and guides, visit **[aigentic.io](https://aigentic.io)**

- [Getting Started](https://aigentic.io/docs/getting-started)
- [Agent DSL](https://aigentic.io/docs/dsl/agent)
- [Tools & Integrations](https://aigentic.io/docs/dsl/tools)
- [Provider Configuration](https://aigentic.io/docs/dsl/providers)

## Examples

Explore ready-to-use examples in the [Aigentic Initializr repository](https://github.com/flock-community/aigentic-initializr) or check our [example projects](src/examples/).

## Development

### Using Snapshots

To use SNAPSHOT versions, add the Sonatype snapshot repository:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
}
```

### Building

```bash
./gradlew -q build
```

### Claude Code cloud sessions

A cloud session starts with an empty `~/.gradle` and downloads every dependency from Maven Central, which often
answers with `429 Too Many Requests`. Two things avoid that:

- [`maven-central-mirror.init.gradle.kts`](.github/gradle/maven-central-mirror.init.gradle.kts) routes Maven Central
  traffic through Google's mirror, which doesn't throttle.
- The [Gradle dependency cache](.github/workflows/gradle-dep-cache.yml) workflow publishes a Gradle
  [read-only dependency cache](https://docs.gradle.org/current/userguide/dependency_caching.html#sec:shared-readonly-cache)
  weekly as an asset of the `gradle-dep-cache` release. It's built through the mirror, because Gradle caches
  dependencies per repository URL, so use the two together.

To use them, in Claude Code (desktop or web) edit the settings of your cloud environment:

- Environment variable: `GRADLE_RO_DEP_CACHE=/opt/gradle-ro-dep-cache`
- Setup script:

  ```bash
  #!/bin/bash
  mkdir -p ~/.gradle/init.d /opt/gradle-ro-dep-cache
  curl -fsSL --retry 3 -o ~/.gradle/init.d/maven-central-mirror.init.gradle.kts \
    https://raw.githubusercontent.com/flock-community/aigentic/main/.github/gradle/maven-central-mirror.init.gradle.kts \
    || echo "Maven Central mirror not installed"
  curl -fsSL --retry 3 https://github.com/flock-community/aigentic/releases/download/gradle-dep-cache/gradle-ro-dep-cache.tar.gz \
    | tar -xz -C /opt/gradle-ro-dep-cache \
    || echo "Gradle dependency cache not downloaded; Gradle falls back to the network"
  ```

The setup script's result is cached with the environment for about a week, so sessions don't download the cache each
time. Dependencies missing from the cache are still downloaded as usual, through the mirror. An environment can use
one read-only cache, so use a separate environment for another Gradle project that publishes its own.

## License

Aigentic is released under the MIT License. See [LICENSE](LICENSE) for details.

## Support

For questions, issues, or feature requests:
- 📖 Visit [aigentic.io](https://aigentic.io) for documentation
- 🐛 Open an issue on our [GitHub repository](https://github.com/flock-community/aigentic/issues)
- 📧 Contact us at [info@aigentic.io](mailto:info@aigentic.io?subject=Aigentic)
