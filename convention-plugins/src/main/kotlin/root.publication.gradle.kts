plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

allprojects {
    group = "community.flock.aigentic"
    version = "0.0.2 "
}

nexusPublishing {

    val isSnapshot = version.toString().endsWith("-SNAPSHOT")
    val url = if (isSnapshot) "https://s01.oss.sonatype.org/content/repositories/snapshots/" else "https://s01.oss.sonatype.org/service/local/"


//    if(!isSnapshot) useStaging.set(false)

    repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri(url))
            snapshotRepositoryUrl.set(uri(url))
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}
