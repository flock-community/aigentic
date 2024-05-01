plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
    linuxX64()
    js(IR) {
        nodejs()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":src:core"))
                implementation(project(":src:providers:openai"))
                // CIO is for JVM, Android, Native. For other platforms pick the correct engine: https://ktor.io/docs/client-engines.html#platforms
                implementation(libs.ktor.client.cio)
            }
        }
    }
}
