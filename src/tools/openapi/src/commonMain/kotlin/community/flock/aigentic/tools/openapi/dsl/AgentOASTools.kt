package community.flock.aigentic.tools.openapi.dsl

import community.flock.aigentic.tools.http.Header
import community.flock.aigentic.tools.http.RestClientExecutor
import community.flock.aigentic.tools.http.toToolDefinition
import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.AgentDSL
import community.flock.aigentic.core.dsl.Config
import community.flock.aigentic.tools.openapi.OpenAPIv3Parser

fun AgentConfig.openApiTools(
    oasJson: String,
    restClientExecutor: RestClientExecutor = RestClientExecutor.default,
    oasHeaderConfig: (OASHeaderConfig.() -> Unit)? = null
) {
    val headerConfig = oasHeaderConfig?.let { OASHeaderConfig().apply(it) }
    val operations = OpenAPIv3Parser.parseOperations(oasJson)

    operations.forEach { operation ->
        val toolDefinition = operation.toToolDefinition(
            restClientExecutor = restClientExecutor,
            headers = headerConfig?.build() ?: emptyList()
        )
        addTool(toolDefinition)
    }
}

@AgentDSL
class OASHeaderConfig : Config<List<Header>> {

    private val headers = mutableListOf<Header>()

    fun OASHeaderConfig.addHeader(header: Header) {
        headers.add(header)
    }

    override fun build(): List<Header> = headers
}
