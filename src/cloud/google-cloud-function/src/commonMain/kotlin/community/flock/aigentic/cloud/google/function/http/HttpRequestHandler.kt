package community.flock.aigentic.cloud.google.function.http

import community.flock.aigentic.cloud.google.function.declarations.GoogleRequest
import community.flock.aigentic.cloud.google.function.http.dsl.Authentication
import community.flock.aigentic.cloud.google.function.http.dsl.GoogleHttpCloudFunction
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.dsl.AgentConfig

internal suspend fun <I, O> GoogleHttpCloudFunction<I, O>.handleRequest(
    googleRequest: GoogleRequest,
    response: dynamic,
) {
    val request = googleRequest.map()

    when (val authentication = authentication) {
        is Authentication.AuthorizationHeader -> {
            val authorization = request.headers["authorization"]
            if (authorization != "Bearer ${authentication.key}") {
                console.error("Invalid authorization header")
                response.status(401).send("Unauthorized")
                throw Exception("Unauthorized")
            }
        }

        else -> {
            // No authentication
        }
    }

    val interceptedRequest =
        try {
            requestInterceptor(request)
        } catch (e: Exception) {
            console.error("Error in request interceptor: ${e.message}")
            response.status(500).send("Internal server error")
            throw Exception("Internal server error")
        }

    val agent = AgentConfig<I, O>().apply { agentBuilder(this, interceptedRequest) }.build()
    val run =
        agent.start().also {
            console.log("Agent finished with result: ${it.result}")
        }

    when (val result = run.result) {
        is Result.Finished<*> -> response.status(200).send(result.response ?: result.description)
        is Result.Stuck -> response.status(422).send(result.reason)
        is Result.Fatal -> {
            console.error("Fatal: ${result.message}")
            response.status(500).send("Internal Server Error")
        }
    }
}
