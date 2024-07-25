import community.flock.wirespec.compiler.core.emit.KotlinEmitter
import community.flock.wirespec.compiler.core.emit.shared.KotlinShared
import community.flock.wirespec.compiler.core.emit.transformer.ClassModelTransformer.transform
import community.flock.wirespec.compiler.core.parse.AST
import community.flock.wirespec.compiler.core.parse.Refined
import community.flock.wirespec.compiler.core.parse.Type
import community.flock.wirespec.compiler.core.parse.Union
import community.flock.wirespec.plugin.gradle.CustomWirespecTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.wirespec)
    id("module.publication")
}


val wirespecKotlin = tasks.register<CustomWirespecTask>("wirespec-kotlin") {
    input = layout.projectDirectory.dir("./wirespec")
    output = layout.buildDirectory.dir("generated")
    packageName = "community.flock.aigentic.gateway.wirespec"
    emitter = KotlinSerializableEmitter::class.java
    shared = KotlinShared.source
    extension = "kt"
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        val commonMain by getting {
            kotlin {
                srcDir(wirespecKotlin.get().output)
            }
            dependencies {
                implementation(project(":src:core"))
                implementation(project(":src:providers:jsonschema"))
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
                implementation(libs.logback.classic)
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotlin.reflect)
                implementation(libs.ktor.client.mock)
                implementation(libs.mockk)
            }
        }
    }
}

class KotlinSerializableEmitter : KotlinEmitter("community.flock.aigentic.gateway.wirespec") {

    override fun Type.emit(ast: AST) = """
    |@kotlinx.serialization.Serializable
    |${transform(ast).emit()}
    """.trimMargin()

    override fun Refined.emit() = """
    |@kotlinx.serialization.Serializable
    |${transform().emit()}
    """.trimMargin()

    override fun Union.emit() = """
    |@kotlinx.serialization.Serializable
    |${transform().emit()}
    """.trimMargin()

}
