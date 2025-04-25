import org.gradle.api.file.DuplicatesStrategy

plugins {
    `kotlin-dsl`
    //id("module.publication")
}

repositories {
    mavenCentral()
    google()
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.9.23-1.0.20")
}

gradlePlugin {
    plugins {
        create("aigentic") {
            id = "aigentic"
            implementationClass = "community.flock.aigentic.code.generation.ksp.gradle.AigenticPlugin"
        }
    }
}
