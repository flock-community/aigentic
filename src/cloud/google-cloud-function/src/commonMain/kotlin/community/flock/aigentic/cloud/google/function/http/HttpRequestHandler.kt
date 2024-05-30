package community.flock.aigentic.cloud.google.function.http

import community.flock.aigentic.cloud.google.function.declarations.GoogleRequest
import community.flock.aigentic.cloud.google.function.http.dsl.Authentication
import community.flock.aigentic.cloud.google.function.http.dsl.GoogleHttpCloudFunction
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.FinishReason.FinishedTask
import community.flock.aigentic.core.agent.tool.FinishReason.ImStuck
import community.flock.aigentic.core.dsl.AgentConfig

internal suspend fun GoogleHttpCloudFunction.handleRequest(
    googleRequest: GoogleRequest,
    response: dynamic,
) {
    val request = googleRequest.map()

    when (val authentication = authentication) {
        is Authentication.AuthorizationHeader -> {
            val authorization = request.headers["authorization"]
            if (authorization != "Bearer ${authentication.key}") {
                response.status(401).send("Unauthorized")
                throw Exception("Unauthorized")
            }
        }

        else -> {
            // No authentication
        }
    }

    val agent = AgentConfig().apply { agentBuilder(this, request) }.build()
    try {
        val run = agent.start()
        val responseText = run.result.response ?: run.result.description

        when (run.result.reason) {
            FinishedTask -> response.status(200).send(responseText)
            ImStuck -> response.status(422).send(responseText)
        }
    } catch (e: Exception) {
        response.status(500).send("Internal Server Error: ${e.message}")
    }
}
