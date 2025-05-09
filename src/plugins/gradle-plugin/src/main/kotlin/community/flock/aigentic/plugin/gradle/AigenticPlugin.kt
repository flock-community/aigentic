package community.flock.aigentic.plugin.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class AigenticPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<AigenticExtension>("aigentic")

        project.plugins.apply("com.google.devtools.ksp")

        project.afterEvaluate {
            project.dependencies {
                add("kspCommonMainMetadata", project.project(":src:code-generation:ksp-processor"))
                add("kspJvm", project.project(":src:code-generation:ksp-processor"))
            }

            project.extensions.getByType<KotlinMultiplatformExtension>().apply {
                sourceSets.getByName("commonMain").kotlin.srcDir(extension.generateSourceDir)
            }

            project.tasks.named("compileKotlinJvm") {
                dependsOn("kspCommonMainKotlinMetadata")
            }

            project.tasks.named("compileKotlinJs") {
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }
    }
}
