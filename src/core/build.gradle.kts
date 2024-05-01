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
        val commonMain by getting {
            dependencies {
                api(libs.coroutines.core)
                api(libs.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
