pluginManagement {
    includeBuild("src/plugins/convention-plugins")
    includeBuild("src/plugins/gradle-plugin")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "aigentic"
include(
    "src:core",
    "src:code-generation:annotations",
    "src:code-generation:ksp-processor",
    "src:code-generation:ksp-test",
    "src:tools:http",
    "src:tools:openapi",
    "src:platform",
    "src:providers:openai",
    "src:providers:gemini",
    "src:providers:ollama",
    "src:providers:jsonschema",
    "src:cloud:google-cloud-function",
    "src:examples:simple-tools",
    "src:examples:openapi-tools",
    "src:examples:google-http-cloud-function",
    "src:examples:testing"
)
