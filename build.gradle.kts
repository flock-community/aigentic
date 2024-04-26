import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

plugins {
    id("root.publication")
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.dokka)
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
