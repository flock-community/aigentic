package community.flock.aigentic.cloud.google.httpcloudfunction

@JsModule("@google-cloud/functions-framework")
@JsNonModule
external val functions: dynamic

external val process: dynamic

fun getEnv(name: String): String = process.env[name].unsafeCast<String>()
