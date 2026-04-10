import community.flock.wirespec.compiler.core.emit.PackageName
import community.flock.wirespec.compiler.core.parse.ast.Module
import community.flock.wirespec.compiler.core.parse.ast.Refined
import community.flock.wirespec.compiler.core.parse.ast.Type
import community.flock.wirespec.emitters.kotlin.KotlinEmitter
import community.flock.wirespec.plugin.gradle.CompileWirespecTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.wirespec)
    id("module.publication")
}

buildscript {
    dependencies {
        classpath(libs.wirespec.compiler)
        classpath(libs.wirespec.emitters.kotlin)
    }
}


val wirespecKotlin = tasks.register<CompileWirespecTask>("wirespec-kotlin") {
    input = layout.projectDirectory.dir("./wirespec")
    output = layout.buildDirectory.dir("generated")
    packageName = "community.flock.aigentic.gateway.wirespec"
    emitterClass = KotlinSerializableEmitter::class.java
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }

        val commonMain by getting {
            kotlin {
                srcDir(wirespecKotlin.get().output)
            }
            dependencies {
                implementation(project(":src:core"))
                implementation(project(":src:providers:jsonschema"))
                implementation(libs.coroutines.core)
                implementation(libs.serialization.json)
                implementation(libs.wirespec.integration)
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
                implementation(libs.logback.classic)
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotlin.reflect)
                implementation(libs.ktor.client.mock)
                implementation(libs.mockk)
            }
        }
    }
}

class KotlinSerializableEmitter : KotlinEmitter(PackageName("community.flock.aigentic.gateway.wirespec")) {

    override fun emit(type: Type, module: Module): String = """
        |@kotlinx.serialization.Serializable
        |${super.emit(type, module)}
    """.trimMargin()

    override fun emit(refined: Refined): String = """
        |@kotlinx.serialization.Serializable
        |${super.emit(refined)}
    """.trimMargin()
}
