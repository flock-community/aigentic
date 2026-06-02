import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin

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

allprojects {
    plugins.withType<NodeJsPlugin> {
        the<NodeJsEnvSpec>().version.set("22.0.0")
    }
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
