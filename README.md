[![stability-wip](https://img.shields.io/badge/stability-wip-lightgrey.svg)](https://github.com/mkenney/software-guides/blob/master/STABILITY-BADGES.md#work-in-progress)
![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/flock-community/aigentic/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/community.flock.aigentic/http-tools?color=blue&label=Download)](https://central.sonatype.com/namespace/community.flock.aigentic)
[![License](https://img.shields.io/github/license/flock-community/aigentic?color=yellow)](LICENSE)
[![Documentation](https://img.shields.io/badge/docs-api-a97bff.svg?logo=kotlin)](https://flock-community.github.io/aigentic/)

# AIGENTIC

Framework to configure and run AI agents


## How to use

To build an agent with local (non-http) tools use the following dependencies:

```
implementation("community.flock.aigentic:core:<version>")
implementation("community.flock.aigentic:openai:<version>") // Also available: community.flock.aigentic:gemini:<version> 
// CIO is for JVM, Android, Native. For other platforms pick the correct engine: https://ktor.io/docs/client-engines.html#platforms
implementation(libs.ktor.client.cio)
```

## Examples

- [Local tool agent examples](src/examples/simple-tools/src/jvmMain/kotlin/community/flock/aigentic/example/RunExamples.kt)
- [OpenAPI tools agent example](src/examples/openapi-tools/src/commonMain/kotlin/community/flock/aigentic/example/OpenAPIAgentExample.kt)
- [GCP Cloud Function agent example](src/examples/google-http-cloud-function/src/jsMain/kotlin/community/flock/aigentic/cloud/google/httpcloudfunction/GoogleHttpCloudFunctionExample.kt)

### How to use snapshots

In order to use SNAPSHOT versions of Aigentic please make sure both maven central and the Sonatype snapshot repository are configured:

```
repositories {
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
}
```
