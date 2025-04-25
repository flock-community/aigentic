plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {

    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":src:core"))
                implementation(project(":src:code-generation:annotations"))
                implementation("com.google.devtools.ksp:symbol-processing-api:1.9.23-1.0.20")
            }
        }
    }

}
