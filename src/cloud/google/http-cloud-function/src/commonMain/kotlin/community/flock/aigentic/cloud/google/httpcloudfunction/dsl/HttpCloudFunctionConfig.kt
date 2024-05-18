package community.flock.aigentic.cloud.google.httpcloudfunction.dsl

import community.flock.aigentic.cloud.google.httpcloudfunction.declarations.functions
import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.AgentDSL
import community.flock.aigentic.core.dsl.Config
import community.flock.aigentic.core.dsl.builderPropertyMissingErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun googleHttpCloudFunction(config: HttpCloudFunctionConfig.() -> Unit) = HttpCloudFunctionConfig().apply(config).build()

sealed interface Authentication {
    data class AuthorizationHeader(val key: String) : Authentication
}

@AgentDSL
class HttpCloudFunctionConfig : Config<Unit> {
    internal var entryPoint: String? = "runAgent"
    internal var agent: Agent? = null
    internal var authentication: Authentication? = null

    fun agent(agentConfig: AgentConfig.() -> Unit): Agent = AgentConfig().apply(agentConfig).build().also { agent = it }

    fun entryPoint(entryPoint: String) {
        this.entryPoint = entryPoint
    }

    override fun build() {
        val function =
            HttpCloudFunction(
                entryPoint = checkNotNull(entryPoint, builderPropertyMissingErrorMessage("entryPoint", "entryPoint()")),
                agent = checkNotNull(agent, builderPropertyMissingErrorMessage("agent", "agent()")),
                authentication = authentication,
            )

        registerHttp(function)
    }

    fun authentication(authentication: Authentication) {
        this.authentication = authentication
    }

    private fun registerHttp(function: HttpCloudFunction) {
        functions.http(function.entryPoint) { request, response ->

            when (val authentication = function.authentication) {
                is Authentication.AuthorizationHeader -> {
                    val authorization = request.headers["Authorization"]
                    if (authorization != "Bearer ${authentication.key}") {
                        response.status(401).send("Unauthorized")
                    }
                }
                else -> {
                    // No authentication
                }
            }

            CoroutineScope(Dispatchers.Default).launch {
                val run = function.agent.start()
                println("Agent finished: $run")
                response.send(run.result.description)
            }
        }
    }
}

data class HttpCloudFunction(
    val entryPoint: String,
    val agent: Agent,
    val authentication: Authentication?,
)
