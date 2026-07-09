plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":src:core"))
                implementation(project(":src:platform"))
                implementation(project(":src:integrations:koog"))
                implementation(libs.koog.agents)
                implementation(libs.logback.classic)
            }
        }
    }
}
