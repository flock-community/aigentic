package community.flock.aigentic.tools.http

import kotlinx.serialization.json.JsonObject

class RestClientExecutor(
    val restClient: RestClient,
    val urlArgumentsResolver: UrlArgumentsResolver,
    val queryParametersArgumentsResolver: QueryParametersArgumentsResolver,
    val requestBodyArgumentsResolver: RequestBodyArgumentsResolver,
) {
    suspend fun execute(
        operation: EndpointOperation,
        callArguments: JsonObject,
        headers: List<Header>,
    ): String =
        restClient.execute(
            method = operation.method,
            resolvedUrl = urlArgumentsResolver.resolveUrl(operation.url, operation.pathParams, callArguments),
            resolvedQueryParameters =
                queryParametersArgumentsResolver.resolveQueryParameters(
                    operation.queryParams,
                    callArguments,
                ),
            resolvedRequestBody =
                operation.requestBody?.let {
                    requestBodyArgumentsResolver.resolveRequestBody(
                        requestBodyParameter = it,
                        callArguments = callArguments,
                    )
                },
            headers = headers,
        )

    companion object {
        val default =
            RestClientExecutor(
                restClient = KtorRestClient(),
                urlArgumentsResolver = DefaultUrlArgumentsResolver(),
                queryParametersArgumentsResolver = DefaultQueryParametersArgumentsResolver(),
                requestBodyArgumentsResolver = DefaultRequestBodyArgumentsResolver(),
            )
    }
}
