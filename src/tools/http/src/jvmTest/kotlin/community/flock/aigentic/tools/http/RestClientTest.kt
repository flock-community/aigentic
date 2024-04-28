package http

import community.flock.aigentic.tools.http.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import java.util.*

class RestClientTest : DescribeSpec({

    val testUrl = ResolvedUrl("https://some-url.com")

    describe("Happy path") {

        context("should map method") {
            withData(
                EndpointOperation.Method.GET to HttpMethod.Get,
                EndpointOperation.Method.POST to HttpMethod.Post,
                EndpointOperation.Method.PUT to HttpMethod.Put,
                EndpointOperation.Method.DELETE to HttpMethod.Delete,
                EndpointOperation.Method.PATCH to HttpMethod.Patch,
            ) { (endpointOperationMethod, ktorMethod) ->

                KtorRestClient(requestAssertions {
                    it.method shouldBe ktorMethod
                }).execute(
                    method = endpointOperationMethod,
                    resolvedUrl = testUrl,
                    resolvedQueryParameters = ResolvedQueryParameters.empty(),
                    resolvedRequestBody = null,
                    headers = emptyList()
                )
            }
        }

        context("should add body if there is one") {
            withData(
                ResolvedRequestBody("""{ "name" : "someValue" }"""), null
            ) { resolvedRequestBody ->

                KtorRestClient(requestAssertions { request: HttpRequestData ->

                    if (resolvedRequestBody != null) {
                        (request.body as TextContent).let {
                            it.contentType.contentType shouldBe "application"
                            it.contentType.contentSubtype shouldBe "json"
                            it.text shouldBe resolvedRequestBody.stringBody
                        }
                    } else {
                        request.body shouldBe EmptyContent
                    }

                }).execute(
                    method = EndpointOperation.Method.GET,
                    resolvedUrl = testUrl,
                    resolvedQueryParameters = ResolvedQueryParameters.empty(),
                    resolvedRequestBody = resolvedRequestBody,
                    headers = emptyList()
                )

            }
        }

        context("should add all query parameters") {

            withData(
                nameFn = { it.values.toString() },
                ResolvedQueryParameters(
                    mapOf(
                        "itemId" to JsonPrimitive(123)
                    )
                ),
                ResolvedQueryParameters(
                    mapOf(
                        "name" to JsonPrimitive("someName")
                    )
                ),
                ResolvedQueryParameters(
                    mapOf(
                        "itemId" to JsonPrimitive(123),
                        "name" to JsonPrimitive("someName"),
                    )
                ),
                ResolvedQueryParameters(
                    mapOf(
                        "isAvailable" to JsonPrimitive(true)
                    )
                ),
                ResolvedQueryParameters(
                    mapOf(
                        "ids" to JsonArray(listOf(JsonPrimitive(1), JsonPrimitive(2), JsonPrimitive(3)))
                    )
                ),
            ) { resolvedQueryParameters ->
                KtorRestClient(requestAssertions { request: HttpRequestData ->
                    request.url.parameters.entries().size shouldBe resolvedQueryParameters.values.size
                }).execute(
                    method = EndpointOperation.Method.GET,
                    resolvedUrl = testUrl,
                    resolvedQueryParameters = resolvedQueryParameters,
                    resolvedRequestBody = null,
                    headers = emptyList()
                )

            }
        }

        it("should add multiple url query components for array query parameters") {

            val resolvedQueryParameters = ResolvedQueryParameters(
                mapOf(
                    "ids" to JsonArray(listOf(JsonPrimitive(1), JsonPrimitive(2), JsonPrimitive(3)))
                )
            )
            KtorRestClient(requestAssertions { request: HttpRequestData ->
                request.url.toString() shouldBe "https://some-url.com?ids=1&ids=2&ids=3"
            }).execute(
                method = EndpointOperation.Method.GET,
                resolvedUrl = testUrl,
                resolvedQueryParameters = resolvedQueryParameters,
                resolvedRequestBody = null,
                headers = emptyList()
            )
        }

        it("should use url") {

            val expectedUrl = "http://localhost/some-url"

            KtorRestClient(requestAssertions { request ->
                request.url.toString() shouldBe expectedUrl
            }).execute(
                method = EndpointOperation.Method.GET,
                resolvedUrl = ResolvedUrl(expectedUrl),
                resolvedQueryParameters = ResolvedQueryParameters.empty(),
                resolvedRequestBody = null,
                headers = emptyList()
            )
        }

        it("should use json header") {

            KtorRestClient(requestAssertions { request ->
                request.headers["Content-Type"] shouldBe "application/json"
            }).execute(
                method = EndpointOperation.Method.GET,
                resolvedUrl = testUrl,
                resolvedQueryParameters = ResolvedQueryParameters.empty(),
                resolvedRequestBody = null,
                headers = emptyList()
            )
        }

        it("should return result") {

            KtorRestClient(requestAssertions {}).execute(
                method = EndpointOperation.Method.GET,
                resolvedUrl = testUrl,
                resolvedQueryParameters = ResolvedQueryParameters.empty(),
                resolvedRequestBody = null,
                headers = emptyList()
            ) shouldBe expectedResult
        }
    }

})

private val expectedResult = UUID.randomUUID().toString()

private fun requestAssertions(result: String = expectedResult, assertBlock: (request: HttpRequestData) -> Unit) =
    MockEngine { request ->
        assertBlock(request)
        respond(
            content = result,
            status = HttpStatusCode.OK,
        )
    }
