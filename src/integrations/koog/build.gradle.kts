plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("module.publication")
    alias(libs.plugins.dokka)
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":src:core"))
                implementation(project(":src:platform"))
                implementation(libs.koog.agents)
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
