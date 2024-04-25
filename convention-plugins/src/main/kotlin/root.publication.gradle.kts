plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

allprojects {
    group = "community.flock.aigentic"
    version = "0.0.2-SNAPSHOT"
}

nexusPublishing {

//    val isSnapshot = version.toString().endsWith("SNAPSHOT")
//    val url = if (isSnapshot) "https://s01.oss.sonatype.org/content/repositories/snapshots/" else "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"

//    useStaging.set(isSnapshot)
    // Configure maven central repository
    // https://github.com/gradle-nexus/publish-plugin#publishing-to-maven-central-via-sonatype-ossrh
    repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
//            nexusUrl.set(uri(url))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"))
//            snapshotRepositoryUrl.set(uri(url))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}
