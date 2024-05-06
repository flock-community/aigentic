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
            trimTrailingWhitespace()
            endWithNewline()
            ktlint()
        }
    }

//    tasks.named<Test>("jvmTest") {
//        useJUnitPlatform()
//        filter {
//            isFailOnNoMatchingTests = false
//        }
//        testLogging {
//            showExceptions = true
//            showStandardStreams = true
//            events = setOf(
//                org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
//                org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
//            )
//            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
//        }
//    }

    tasks.findByName("jvmTest")?.let { task ->
        task as Test
        task.useJUnitPlatform()
        task.filter {
            isFailOnNoMatchingTests = false
        }
        task.testLogging {
            showExceptions = true
            showStandardStreams = true
            events = setOf(
                org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
            )
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

}

tasks.withType<DokkaMultiModuleTask> {
    outputDirectory.set(projectDir.resolve("docs"))
}
