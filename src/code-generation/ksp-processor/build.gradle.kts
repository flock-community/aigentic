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
                implementation(libs.ksp.api)
            }
        }
    }

}
