plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("module.publication")
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
                api(libs.serialization.core)
            }
        }
    }
}
