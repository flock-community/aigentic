package community.flock.aigentic.cloud.google.function.util

import community.flock.aigentic.cloud.google.function.declarations.GoogleRequest

fun createRequestResponse(invalidAuthorizationHeader: Boolean = false): Pair<GoogleRequest, GoogleResponseWrapper> {
    val headers =
        if (invalidAuthorizationHeader) {
            js("{ authorization: 'Bearer invalid-key' }")
        } else {
            js("{ authorization: 'Bearer some-secret-key' }")
        }

    val googleRequest =
        object : GoogleRequest {
            override val method: String = "GET"
            override val headers: dynamic = headers
            override val query: dynamic = js("{ name: 'John' }")
            override val body: dynamic = js("{ message: 'Hello World'}")
        }

    val googleResponseWrapper = GoogleResponseWrapper()

    return googleRequest to googleResponseWrapper
}

class GoogleResponseWrapper {
    var statusCode: Int? = null
        private set

    var response: String? = null
        private set

    val googleResponse: dynamic = js("{}")

    init {
        googleResponse.status = { code: Int ->
            statusCode = code
            googleResponse
        }

        googleResponse.send = { result: String ->
            response = result
        }
    }
}
