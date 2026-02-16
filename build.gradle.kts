import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("root.publication")
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotest.multiplatform) apply false
    alias(libs.plugins.dokka)
}

subprojects {

    apply(plugin = "com.diffplug.spotless")
    configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**")
            trimTrailingWhitespace()
            endWithNewline()
            ktlint().editorConfigOverride(
                mapOf(
                    "max_line_length" to "160"
                )
            )
        }
    }
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(projectDir.resolve("docs"))
    }
}

dependencies {
    dokka(project(":src:core"))
    dokka(project(":src:tools:http"))
    dokka(project(":src:tools:openapi"))
    dokka(project(":src:platform"))
    dokka(project(":src:providers:openai"))
    dokka(project(":src:providers:gemini"))
    dokka(project(":src:providers:vertexai"))
    dokka(project(":src:providers:ollama"))
    dokka(project(":src:cloud:google-cloud-function"))
}
