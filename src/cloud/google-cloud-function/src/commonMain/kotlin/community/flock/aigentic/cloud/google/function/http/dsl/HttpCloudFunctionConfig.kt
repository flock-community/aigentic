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

inline fun <reified I : Any, O : Any> googleHttpCloudFunction(config: HttpCloudFunctionConfig<I, O>.() -> Unit) =
    HttpCloudFunctionConfig<I, O>().apply(config).build().run {
        registerHttpFunction(this)
    }

@AgentDSL
class HttpCloudFunctionConfig<I : Any, O : Any> : Config<GoogleHttpCloudFunction<I, O>> {
    internal var entryPoint: String? = "runAgent"
    internal var authentication: Authentication? = null
    internal var beforeRequestAction: suspend (request: Request) -> Request = { it }
    internal var agentBuilder: (suspend AgentConfig<I, O>.(request: Request) -> Unit)? = null

    fun agent(agentConfig: suspend AgentConfig<I, O>.(request: Request) -> Unit) {
        agentBuilder = agentConfig
    }

    fun entryPoint(entryPoint: String) {
        this.entryPoint = entryPoint
    }

    fun requestInterceptor(requestInterceptor: suspend (request: Request) -> Request) {
        this.beforeRequestAction = requestInterceptor
    }

    override fun build(): GoogleHttpCloudFunction<I, O> =
        GoogleHttpCloudFunction<I, O>(
            entryPoint = checkNotNull(entryPoint, builderPropertyMissingErrorMessage("entryPoint", "entryPoint()")),
            agentBuilder = checkNotNull(agentBuilder, builderPropertyMissingErrorMessage("agent", "agent()")),
            authentication = authentication,
            requestInterceptor = beforeRequestAction,
        )

    fun authentication(authentication: Authentication) {
        this.authentication = authentication
    }
}

inline fun <reified I : Any, O : Any> registerHttpFunction(function: GoogleHttpCloudFunction<I, O>) {
    functions.http(function.entryPoint) { request, response ->
        CoroutineScope(Dispatchers.Default).launch {
            function.handleRequest(request, response)
        }
    }
}

sealed interface Authentication {
    data class AuthorizationHeader(val key: String) : Authentication
}

data class GoogleHttpCloudFunction<I : Any, O : Any>(
    val entryPoint: String,
    val requestInterceptor: suspend (request: Request) -> Request,
    val agentBuilder: suspend AgentConfig<I, O>.(request: Request) -> Unit,
    val authentication: Authentication?,
)
