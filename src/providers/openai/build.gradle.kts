plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("module.publication")
    alias(libs.plugins.dokka)
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
                implementation(project(":src:tools:jsonschema"))
                implementation(libs.openai.client)
            }
        }
        val commonTest by getting {
            dependencies {
            }
        }
    }
}
