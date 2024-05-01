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
                api(libs.coroutines.core)
                implementation(libs.serialization.json) // FIXME temporary needed for tool Handler arguments
                implementation(project(":src:core"))
                implementation(project(":src:providers:openai"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}
