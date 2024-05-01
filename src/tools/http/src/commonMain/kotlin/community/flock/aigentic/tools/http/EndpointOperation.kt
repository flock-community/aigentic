package community.flock.aigentic.tools.http

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import kotlinx.serialization.json.JsonObject

data class EndpointOperation(
    val name: String,
    val description: String?,
    val method: Method,
    val url: String,
    val pathParams: List<Parameter>,
    val queryParams: List<Parameter>,
    val requestBody: Parameter.Complex.Object?,
) {
    enum class Method { GET, POST, PUT, DELETE, PATCH }
}

fun EndpointOperation.toToolDefinition(restClientExecutor: RestClientExecutor, headers: List<Header>): Tool {

    val allParameterDefinitions = pathParams + queryParams + listOfNotNull(requestBody)

    return object : Tool {
        override val name = ToolName(this@toToolDefinition.name)
        override val description = this@toToolDefinition.description
        override val parameters = allParameterDefinitions
        override val handler: suspend (map: JsonObject) -> String = {
            restClientExecutor.execute(operation = this@toToolDefinition, callArguments = it, headers = headers)
        }
    }
}
