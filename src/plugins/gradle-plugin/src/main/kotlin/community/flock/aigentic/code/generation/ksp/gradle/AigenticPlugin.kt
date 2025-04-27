package community.flock.aigentic.code.generation.ksp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class AigenticPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create extension for configuration
        val extension = project.extensions.create<AigenticExtension>("aigentic")

        // Apply KSP plugin
        project.plugins.apply("com.google.devtools.ksp")

        // Configure the plugin after project evaluation
        project.afterEvaluate {
            // Add KSP dependency
            project.dependencies {
                add("kspCommonMainMetadata", project.project(":src:code-generation:ksp-processor"))
                add("kspJvm", project.project(":src:code-generation:ksp-processor"))
                add("commonMainImplementation", project.project(":src:code-generation:annotations"))
            }

            // Configure Kotlin source sets to include generated code
            project.extensions.getByType<KotlinMultiplatformExtension>().apply {
                sourceSets.getByName("commonMain").kotlin.srcDir(extension.generateSourceDir)
                sourceSets.getByName("jvmMain").kotlin.srcDir(extension.generateJvmSourceDir)
            }

            // Fix task dependencies between KSP and Kotlin compile tasks
            project.tasks.named("compileKotlinJvm") {
                dependsOn("kspCommonMainKotlinMetadata")
            }

            project.tasks.named("compileKotlinJs") {
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }
    }
}
