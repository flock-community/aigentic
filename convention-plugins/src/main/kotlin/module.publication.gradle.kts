import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
    signing
}

publishing {
    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        // Provide artifacts information required by Maven Central
        pom {
            name.set("Aigentic")
            description.set("Aigentic")
            url.set("https://github.com/flock-community/aigentic")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("Aigentic")
                    email.set("info@aigentic.io")
                    name.set("Aigentic Team")
                    organization.set("Aigentic")
                    organizationUrl.set("https://aigentic.io")
                }
            }
            scm {
                url.set("https://github.com/flock-community/aigentic")
            }
        }
    }
}

val isSigningEnabled: Boolean = System.getenv("ENABLE_GRADLE_SIGNING")?.toBoolean() ?: true

if(isSigningEnabled) {
    signing {
        useGpgCmd()
        sign(publishing.publications)
    }
}
