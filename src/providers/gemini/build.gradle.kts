plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("module.publication")
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinx.serialization)
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
                // TODO Temp to test tools schema
                implementation(project(":src:providers:openai"))

                api(libs.coroutines.core)
                implementation(libs.serialization.json)
                api(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.auth)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
            }
        }
    }
}
