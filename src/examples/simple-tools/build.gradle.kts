plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinx.serialization)
    id("aigentic")
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":src:core"))
                implementation(project(":src:platform"))
                implementation(project(":src:providers:openai"))
                implementation(project(":src:providers:gemini"))
                implementation(project(":src:providers:vertexai"))
                implementation(project(":src:providers:ollama"))
                implementation(project(":src:tools:openapi"))
            }
        }

        val jvmMain by getting {
            dependencies {
                // CIO is for JVM, Android, Native. For other platforms pick the correct engine: https://ktor.io/docs/client-engines.html#platforms
                implementation(libs.ktor.client.cio)

                implementation(libs.logback.classic)
                implementation("org.slf4j:slf4j-api:2.0.9")
            }
        }
    }
}
