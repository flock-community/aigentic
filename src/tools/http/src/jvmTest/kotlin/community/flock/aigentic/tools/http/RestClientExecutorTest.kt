package community.flock.aigentic.tools.http

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RestClientExecutorTest : DescribeSpec({

    describe("happy path") {

        it("should execute operation with callArguments") {

            val operation =
                EndpointOperation(
                    name = "getItemsByCategory",
                    description = null,
                    method = EndpointOperation.Method.GET,
                    pathParams =
                        listOf(
                            Parameter.Primitive(
                                name = "id",
                                description = "Item id",
                                isRequired = true,
                                type = ParameterType.Primitive.Integer,
                            ),
                        ),
                    url = "https://example.com/api/items/{id}",
                    queryParams =
                        listOf(
                            Parameter.Primitive(
                                name = "category",
                                description = "The category of the items to retrieve",
                                isRequired = true,
                                type = ParameterType.Primitive.String,
                            ),
                        ),
                    requestBody =
                        Parameter.Complex.Object(
                            name = "body",
                            description = "Item to be created",
                            parameters =
                                listOf(
                                    Parameter.Primitive(
                                        name = "name",
                                        description = null,
                                        isRequired = true,
                                        type = ParameterType.Primitive.String,
                                    ),
                                    Parameter.Primitive(
                                        name = "price",
                                        description = null,
                                        isRequired = true,
                                        type = ParameterType.Primitive.Number,
                                    ),
                                ),
                            isRequired = true,
                        ),
                )

            val headers =
                listOf(
                    Header.CustomHeader("api-key", "secret-value"),
                )

            val callArguments =
                buildJsonObject {
                    put("id", 2)
                    put("category", "furniture")
                }

            val expectedUrl = ResolvedUrl("https://example.com/api/items/2")
            val expectedQueryParameters = ResolvedQueryParameters(mapOf("category" to JsonPrimitive("furniture")))
            val expectedRequestBody = ResolvedRequestBody("""{ "name" : "someName", "price" : 12 }""")

            val restClient =
                mockk<RestClient>().apply {
                    coEvery {
                        execute(
                            method = operation.method,
                            resolvedUrl = expectedUrl,
                            resolvedQueryParameters = expectedQueryParameters,
                            resolvedRequestBody = expectedRequestBody,
                            headers = headers,
                        )
                    } returns "someResult"
                }

            val urlArgumentsResolver =
                mockk<UrlArgumentsResolver>().apply {
                    every { resolveUrl(operation.url, operation.pathParams, callArguments) } returns expectedUrl
                }

            val queryParametersArgumentsResolver =
                mockk<QueryParametersArgumentsResolver>().apply {
                    every { resolveQueryParameters(operation.queryParams, callArguments) } returns expectedQueryParameters
                }

            val requestBodyArgumentsResolver =
                mockk<RequestBodyArgumentsResolver>().apply {
                    every { resolveRequestBody(operation.requestBody!!, callArguments) } returns expectedRequestBody
                }

            val executor =
                RestClientExecutor(
                    restClient = restClient,
                    urlArgumentsResolver = urlArgumentsResolver,
                    queryParametersArgumentsResolver = queryParametersArgumentsResolver,
                    requestBodyArgumentsResolver = requestBodyArgumentsResolver,
                )

            executor.execute(
                operation = operation,
                callArguments = callArguments,
                headers = headers,
            ) shouldBe "someResult"

            coVerify(exactly = 1) { restClient.execute(any(), any(), any(), any(), any()) }
            verify(exactly = 1) { urlArgumentsResolver.resolveUrl(any(), any(), any()) }
            verify(exactly = 1) { queryParametersArgumentsResolver.resolveQueryParameters(any(), any()) }
            verify(exactly = 1) { requestBodyArgumentsResolver.resolveRequestBody(any(), any()) }
        }
    }
})
