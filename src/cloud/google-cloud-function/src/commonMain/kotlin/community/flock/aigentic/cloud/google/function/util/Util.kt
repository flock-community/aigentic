package community.flock.aigentic.cloud.google.function.util

import community.flock.aigentic.cloud.google.function.declarations.process

fun getEnvVar(name: String): String = process.env[name].unsafeCast<String>()
