plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    js(IR) {
        nodejs()
        generateTypeScriptDefinitions()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":src:core"))
                implementation(project(":src:providers:openai"))
                implementation(project(":src:cloud:google:http-cloud-function"))
                implementation ("ch.qos.logback:logback-classic:1.2.3")
            }
        }
    }
}
