package community.flock.aigentic.cloud.google.function.http.dsl

import community.flock.aigentic.cloud.google.function.declarations.functions
import community.flock.aigentic.cloud.google.function.http.Request
import community.flock.aigentic.cloud.google.function.http.handleRequest
import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.AgentDSL
import community.flock.aigentic.core.dsl.Config
import community.flock.aigentic.core.dsl.builderPropertyMissingErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun googleHttpCloudFunction(config: HttpCloudFunctionConfig.() -> Unit) = HttpCloudFunctionConfig().apply(config).build().run { registerHttpFunction(this) }

@AgentDSL
class HttpCloudFunctionConfig : Config<GoogleHttpCloudFunction> {
    internal var entryPoint: String? = "runAgent"
    internal var authentication: Authentication? = null
    internal var beforeRequestAction: (request: Request) -> Request = { it }
    internal var agentBuilder: (AgentConfig.(request: Request) -> Unit)? = null

    fun agent(agentConfig: AgentConfig.(request: Request) -> Unit) {
        agentBuilder = agentConfig
    }

    fun entryPoint(entryPoint: String) {
        this.entryPoint = entryPoint
    }

    fun requestInterceptor(requestInterceptor: (request: Request) -> Request) {
        this.beforeRequestAction = requestInterceptor
    }

    override fun build(): GoogleHttpCloudFunction =
        GoogleHttpCloudFunction(
            entryPoint = checkNotNull(entryPoint, builderPropertyMissingErrorMessage("entryPoint", "entryPoint()")),
            agentBuilder = checkNotNull(agentBuilder, builderPropertyMissingErrorMessage("agent", "agent()")),
            authentication = authentication,
            requestInterceptor = beforeRequestAction,
        )

    fun authentication(authentication: Authentication) {
        this.authentication = authentication
    }
}

private fun registerHttpFunction(function: GoogleHttpCloudFunction) {
    functions.http(function.entryPoint) { request, response ->
        CoroutineScope(Dispatchers.Default).launch {
            function.handleRequest(request, response)
        }
    }
}

sealed interface Authentication {
    data class AuthorizationHeader(val key: String) : Authentication
}

data class GoogleHttpCloudFunction(
    val entryPoint: String,
    val requestInterceptor: (request: Request) -> Request,
    val agentBuilder: AgentConfig.(request: Request) -> Unit,
    val authentication: Authentication?,
)
