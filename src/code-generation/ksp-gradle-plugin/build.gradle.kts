import org.gradle.api.file.DuplicatesStrategy

plugins {
    `kotlin-dsl`
//    id("module.publication")
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
}

gradlePlugin {
    plugins {
        create("aigentic") {
            id = "aigentic"
            implementationClass = "community.flock.aigentic.code.generation.ksp.gradle.AigenticPlugin"
        }
    }
}
