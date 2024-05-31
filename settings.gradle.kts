pluginManagement {
    includeBuild("convention-plugins")
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
    "src:tools:jsonschema",
    "src:tools:http",
    "src:tools:openapi",
    "src:providers:openai",
    "src:providers:gemini",
    "src:cloud:google-cloud-function",
    "src:examples:simple-tools",
    "src:examples:openapi-tools",
    "src:examples:google-http-cloud-function"
)
