plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    id("module.publication")
}

dependencies {
    implementation(libs.ksp.api)
    implementation(kotlin("stdlib"))
}
