plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("module.publication")
    alias(libs.plugins.dokka)
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    linuxX64()
    js(IR) {
        nodejs()
        generateTypeScriptDefinitions()
    }

    sourceSets {

        all {
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }

        val commonMain by getting {
            dependencies {
                api(libs.coroutines.core)
                implementation(libs.serialization.json)
                implementation(libs.kotlinx.datetime)
                api(libs.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.datatest)
                implementation(libs.kotest.property)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotlin.reflect)
                implementation(libs.mockk)
            }
        }
    }
}
