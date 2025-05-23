import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

plugins {
    id("root.publication")
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.spotless) apply false
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

tasks.withType<DokkaMultiModuleTask> {
    outputDirectory.set(projectDir.resolve("docs"))
}
