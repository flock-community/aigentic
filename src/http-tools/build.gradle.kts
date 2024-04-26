plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("module.publication")
    id("org.jetbrains.dokka")
}

kotlin {

    jvm()
    linuxX64()
    js(IR) {
        nodejs()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
