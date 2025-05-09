plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("module.publication")
}

kotlin {

    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":src:core"))
                implementation(libs.ksp.api)
            }
        }
    }

}
