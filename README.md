[![stability-wip](https://img.shields.io/badge/stability-wip-lightgrey.svg)](https://github.com/mkenney/software-guides/blob/master/STABILITY-BADGES.md#work-in-progress)
![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/flock-community/aigentic/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/community.flock.aigentic/http-tools?color=blue&label=Download)](https://central.sonatype.com/namespace/community.flock.aigentic)
[![License](https://img.shields.io/github/license/flock-community/aigentic?color=yellow)](LICENSE)
[![Documentation](https://img.shields.io/badge/docs-api-a97bff.svg?logo=kotlin)](https://flock-community.github.io/aigentic/)

# AIGENTIC

Framework to configure and run AI agents


## How to use

To build an agent which uses OpenAI as model provider with local (non http tools) use the following dependencies:

```
implementation("community.flock.aigentic:core:0.0.4-SNAPSHOT")
implementation("community.flock.aigentic:openai:0.0.4-SNAPSHOT")
implementation("io.ktor:ktor-client-okhttp:2.3.10")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
```

## Example agent

[AdministrativeAgentExample](src/example/src/commonMain/kotlin/community/flock/aigentic/example/AdministrativeAgentExample.kt)

### How to use snapshots

In order to use SNAPSHOT versions of Aigentic please make sure both maven central and the Sonatype snapshot repository are configured:

```
repositories {
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
}
```