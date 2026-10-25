// Routes Maven Central traffic through Google's mirror, which doesn't throttle like Maven Central does.
// The mirror goes first in the settings repositories because the Gradle Plugin Portal redirects
// most artifact requests to Maven Central. Used by Claude Code cloud sessions and by
// .github/workflows/gradle-dep-cache.yml, so the read-only cache matches the sessions' repositories.
val mirrorUrl = "https://maven-central.storage-download.googleapis.com/maven2/"
val centralHosts = setOf("repo.maven.apache.org", "repo1.maven.org")

fun RepositoryHandler.redirectCentral() =
    withType<MavenArtifactRepository>().configureEach {
        if (url.host in centralHosts) setUrl(mirrorUrl)
    }

fun RepositoryHandler.preferMirror() {
    if (isNotEmpty()) {
        val mirror = maven {
            name = "MavenCentralMirror"
            setUrl(mirrorUrl)
        }
        remove(mirror)
        addFirst(mirror)
    }
}

beforeSettings {
    pluginManagement.repositories.redirectCentral()
    dependencyResolutionManagement.repositories.redirectCentral()
}

settingsEvaluated {
    pluginManagement.repositories.preferMirror()
    dependencyResolutionManagement.repositories.preferMirror()
}

allprojects {
    buildscript.repositories.redirectCentral()
    repositories.redirectCentral()
}
