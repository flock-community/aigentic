package community.flock.aigentic.cloud.google.httpcloudfunction.declarations

external interface GoogleRequest {
    val method: String
    val headers: dynamic
    val query: dynamic
    val body: dynamic
}

external interface Functions {
    fun http(
        entryPoint: String,
        handler: (request: GoogleRequest, response: dynamic) -> Unit,
    )
}

@JsModule("@google-cloud/functions-framework")
@JsNonModule
external val functions: Functions

external val process: dynamic
