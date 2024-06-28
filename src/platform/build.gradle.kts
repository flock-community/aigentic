import community.flock.wirespec.plugin.Language
import community.flock.wirespec.plugin.gradle.CompileWirespecTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.wirespec)
    id("module.publication")
}

tasks.compileKotlinMetadata {
    dependsOn("wirespec-kotlin")
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
        generateTypeScriptDefinitions()
    }

    sourceSets {

        all {
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }

        val commonMain by getting {
            kotlin {
                srcDir("${layout.buildDirectory.get()}/generated")
            }
            dependencies {
                implementation(project(":src:core"))
                implementation(libs.coroutines.core)
                implementation(libs.serialization.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.auth)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.datatest)
                implementation(libs.kotest.property)
                implementation(libs.ktor.client.mock)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotlin.reflect)
                implementation(libs.ktor.client.mock)
                implementation(libs.mockk)
            }
        }
    }
}

tasks.register<CompileWirespecTask>("wirespec-kotlin") {
    input = layout.projectDirectory.dir("wirespec")
    output = layout.buildDirectory.dir("generated")
    packageName = "community.flock.aigentic.wirespec"
    languages = listOf(Language.Kotlin)
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    filter {
        isFailOnNoMatchingTests = false
    }
    testLogging {
        showExceptions = true
        showStandardStreams = true
        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
