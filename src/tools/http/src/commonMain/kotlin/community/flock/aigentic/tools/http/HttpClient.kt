package community.flock.aigentic.tools.http

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

interface RestClient {
    suspend fun execute(
        method: EndpointOperation.Method,
        resolvedUrl: ResolvedUrl,
        resolvedQueryParameters: ResolvedQueryParameters,
        resolvedRequestBody: ResolvedRequestBody?,
        headers: List<Header>,
    ): String
}

class KtorRestClient(engine: HttpClientEngine? = null) : RestClient {
    private val configuration: HttpClientConfig<*>.() -> Unit = {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.NONE
        }
    }

    private val ktor = if (engine != null) HttpClient(engine, configuration) else HttpClient(configuration)

    override suspend fun execute(
        method: EndpointOperation.Method,
        resolvedUrl: ResolvedUrl,
        resolvedQueryParameters: ResolvedQueryParameters,
        resolvedRequestBody: ResolvedRequestBody?,
        headers: List<Header>,
    ): String =
        ktor.request(resolvedUrl.urlString) {
            headers {
                headers.forEach { header ->
                    append(header.name, header.value)
                }
            }
            contentType(ContentType.Application.Json)
            this.method = method.toKtorMethod()
            url {
                addQueryParameters(resolvedQueryParameters)
            }
            resolvedRequestBody?.let {
                setBody(it.stringBody)
            }
        }.body<String>()

    private fun URLBuilder.addQueryParameters(resolvedQueryParameters: ResolvedQueryParameters) =
        resolvedQueryParameters.values.forEach { (name, value) ->
            if (value is JsonArray) {
                // TODO make configurable how query parameters are added to the url (i.e.: repeated, comma separated, etc) appendAll results in i.e.: ?ids=1&ids=2&ids=3
                parameters.appendAll(name, value.jsonArray.toList().map { it.jsonPrimitive.content })
            } else {
                parameters.append(name, value.jsonPrimitive.content)
            }
        }

    private fun EndpointOperation.Method.toKtorMethod() =
        when (this) {
            EndpointOperation.Method.GET -> HttpMethod.Get
            EndpointOperation.Method.POST -> HttpMethod.Post
            EndpointOperation.Method.PUT -> HttpMethod.Put
            EndpointOperation.Method.DELETE -> HttpMethod.Delete
            EndpointOperation.Method.PATCH -> HttpMethod.Patch
        }
}
