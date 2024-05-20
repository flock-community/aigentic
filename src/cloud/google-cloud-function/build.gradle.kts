plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    id("module.publication")
}

kotlin {
    js(IR) {
        nodejs()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(npm("@google-cloud/functions-framework", "^3.4.0")) // TODO move version number to toml
                implementation(project(":src:core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.datatest)
            }
        }

    }
}

