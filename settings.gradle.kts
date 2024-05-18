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
    "src:tools:http",
    "src:tools:openapi",
    "src:providers:openai",
    "src:example",
    "src:cloud:google:http-cloud-function",
)
