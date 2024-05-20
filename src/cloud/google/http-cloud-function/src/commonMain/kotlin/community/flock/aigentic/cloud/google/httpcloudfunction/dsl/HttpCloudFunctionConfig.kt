package community.flock.aigentic.cloud.google.httpcloudfunction.dsl

import community.flock.aigentic.cloud.google.httpcloudfunction.declarations.GoogleRequest
import community.flock.aigentic.cloud.google.httpcloudfunction.declarations.functions
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.FinishReason
import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.AgentDSL
import community.flock.aigentic.core.dsl.Config
import community.flock.aigentic.core.dsl.builderPropertyMissingErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun googleHttpCloudFunction(config: HttpCloudFunctionConfig.() -> Unit) = HttpCloudFunctionConfig().apply(config).build()

sealed interface Authentication {
    data class AuthorizationHeader(val key: String) : Authentication
}

@AgentDSL
class HttpCloudFunctionConfig : Config<Unit> {
    internal var entryPoint: String? = "runAgent"
    internal var authentication: Authentication? = null

    var agentBuilder: (AgentConfig.(request: Request) -> Unit)? = null

    fun agent(agentConfig: AgentConfig.(request: Request) -> Unit) {
        agentBuilder = agentConfig
    }

    fun entryPoint(entryPoint: String) {
        this.entryPoint = entryPoint
    }

    override fun build() {
        val function =
            GoogleHttpCloudFunction(
                entryPoint = checkNotNull(entryPoint, builderPropertyMissingErrorMessage("entryPoint", "entryPoint()")),
                agentBuilder = checkNotNull(agentBuilder, builderPropertyMissingErrorMessage("agent", "agent()")),
                authentication = authentication,
            )

        handleRequest(function)
    }

    fun authentication(authentication: Authentication) {
        this.authentication = authentication
    }

    private fun handleRequest(function: GoogleHttpCloudFunction) {
        functions.http(function.entryPoint) { googleRequest, response ->

            val request = googleRequest.map()

            when (val authentication = function.authentication) {
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

            CoroutineScope(Dispatchers.Default).launch {
                val agent = AgentConfig().apply { function.agentBuilder(this, request) }.build()
                val run = agent.start()

                val responseText = run.result.description

                when (run.result.reason) {
                    FinishReason.FinishedTask -> response.status(200).send(responseText)
                    FinishReason.ImStuck -> response.status(422).send(responseText)
                }
            }
        }
    }
}

private fun GoogleRequest.map(): Request =
    Request(
        method = method,
        headers = dynamicObjectToMap(headers),
        query = dynamicObjectToMap(query),
        body = Json.parseToJsonElement(JSON.stringify(body)).jsonObject,
    )

private fun dynamicObjectToMap(jsObject: dynamic): Map<String, String> =
    Json.parseToJsonElement(JSON.stringify(jsObject)).jsonObject
        .mapValues { it.value.jsonPrimitive.content }

data class Request(
    val method: String,
    val headers: Map<String, String>,
    val query: Map<String, String>,
    val body: JsonObject,
)

data class GoogleHttpCloudFunction(
    val entryPoint: String,
    val agentBuilder: AgentConfig.(request: Request) -> Unit,
    val authentication: Authentication?,
)
