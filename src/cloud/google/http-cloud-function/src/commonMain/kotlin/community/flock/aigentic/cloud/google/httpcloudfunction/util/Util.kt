package community.flock.aigentic.cloud.google.httpcloudfunction.util

import community.flock.aigentic.cloud.google.httpcloudfunction.declarations.process

fun getEnvVar(name: String): String = process.env[name].unsafeCast<String>()
