package community.flock.aigentic.tools.http

import http.createObjectParameter
import http.createPrimitiveParameter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class ArgumentsResolverTest :
    DescribeSpec({

        val itemIdParameterName = "itemId"
        val commentIdParameterName = "commentId"

        describe("DefaultUrlArgumentsResolver") {

            val resolver = DefaultUrlArgumentsResolver()

            describe("happy path") {

                it("should replace single path parameters in url") {

                    val jsonArguments =
                        buildJsonObject {
                            put(itemIdParameterName, "123")
                        }

                    val url = "https://localhost/items/{itemId}"

                    val pathParameters =
                        listOf(
                            createPrimitiveParameter(itemIdParameterName),
                        )

                    resolver.resolveUrl(
                        url,
                        pathParameters,
                        jsonArguments,
                    ) shouldBe ResolvedUrl("https://localhost/items/123")
                }

                it("should replace multiple path parameters in url") {

                    val jsonArguments =
                        buildJsonObject {
                            put(itemIdParameterName, "123")
                            put(commentIdParameterName, "456")
                        }

                    val url = "https://localhost/items/{itemId}/comments/{commentId}"

                    val pathParameters =
                        listOf(
                            createPrimitiveParameter(itemIdParameterName),
                            createPrimitiveParameter(commentIdParameterName),
                        )

                    resolver.resolveUrl(
                        url,
                        pathParameters,
                        jsonArguments,
                    ) shouldBe ResolvedUrl("https://localhost/items/123/comments/456")
                }
            }

            describe("exceptions") {

                it("should throw exception when path parameter is not in call arguments") {

                    val jsonArguments =
                        buildJsonObject {
                            put(itemIdParameterName, "123")
                            put("someOtherParam", "456")
                        }

                    val url = "https://localhost/items/{itemId}/comments/{commentId}"

                    val pathParameters =
                        listOf(
                            createPrimitiveParameter(itemIdParameterName),
                            createPrimitiveParameter(commentIdParameterName),
                        )

                    shouldThrow<Exception> {
                        resolver.resolveUrl(url, pathParameters, jsonArguments)
                    }
                }

                it("should throw exception if the number of placeholders in the url is greater than the number of pathParameters") {

                    val jsonArguments =
                        buildJsonObject {
                            put(itemIdParameterName, "123")
                            put(commentIdParameterName, "456")
                        }

                    val url = "https://localhost/items/{itemId}/comments/{commentId}"

                    val pathParameters =
                        listOf(
                            createPrimitiveParameter(itemIdParameterName),
                        )

                    shouldThrow<Exception> {
                        resolver.resolveUrl(url, pathParameters, jsonArguments)
                    }
                }
            }
        }

        describe("DefaultRequestBodyArgumentsResolver") {

            val resolver = DefaultRequestBodyArgumentsResolver()

            describe("Happy path") {

                val bodyParameter =
                    createObjectParameter(
                        name = "body",
                        parameters = listOf(createPrimitiveParameter("someProperty")),
                    )

                val jsonArguments =
                    buildJsonObject {
                        put("body", """{ "someProperty" : "hello" }""")
                    }

                resolver.resolveRequestBody(
                    bodyParameter,
                    jsonArguments,
                ) shouldBe ResolvedRequestBody(""""{ \"someProperty\" : \"hello\" }"""")
            }

            it("should leave out request body fields that are null") {
                val bodyParameter =
                    createObjectParameter(
                        name = "body",
                        parameters = listOf(createPrimitiveParameter("name"), createPrimitiveParameter("tag", isRequired = false)),
                    )

                val jsonArguments =
                    buildJsonObject {
                        putJsonObject("body") {
                            put("name", "Rex")
                            put("tag", JsonNull)
                            putJsonObject("owner") {
                                put("name", "Ann")
                                put("email", JsonNull)
                            }
                        }
                    }

                resolver.resolveRequestBody(
                    bodyParameter,
                    jsonArguments,
                ) shouldBe ResolvedRequestBody("""{"name":"Rex","owner":{"name":"Ann"}}""")
            }

            it("should not resolve a non-required request body that is null") {
                val bodyParameter = createObjectParameter(name = "body", isRequired = false, parameters = emptyList())

                resolver.resolveRequestBody(
                    bodyParameter,
                    buildJsonObject { put("body", JsonNull) },
                ) shouldBe null
            }
        }

        describe("DefaultQueryParametersArgumentsResolver") {

            val resolver = DefaultQueryParametersArgumentsResolver()

            describe("happy path") {
                it("should resolve all query parameter arguments") {

                    val jsonArguments =
                        buildJsonObject {
                            put(itemIdParameterName, "123")
                            put(commentIdParameterName, "456")
                        }

                    val queryParameters =
                        listOf(
                            createPrimitiveParameter(itemIdParameterName),
                        )

                    resolver.resolveQueryParameters(
                        queryParameters,
                        jsonArguments,
                    ) shouldBe
                        ResolvedQueryParameters(
                            mapOf(
                                itemIdParameterName to JsonPrimitive("123"),
                            ),
                        )
                }

                it("should resolve all non-required query parameter arguments when argument is not provided") {

                    val jsonArguments =
                        buildJsonObject {
                            put(commentIdParameterName, "456")
                        }

                    val queryParameters =
                        listOf(
                            createPrimitiveParameter(itemIdParameterName, isRequired = false),
                        )

                    resolver.resolveQueryParameters(
                        queryParameters,
                        jsonArguments,
                    ) shouldBe ResolvedQueryParameters(emptyMap())
                }

                it("should leave out non-required query parameter arguments that are null") {

                    val jsonArguments =
                        buildJsonObject {
                            put(itemIdParameterName, JsonNull)
                        }

                    val queryParameters =
                        listOf(
                            createPrimitiveParameter(itemIdParameterName, isRequired = false),
                        )

                    resolver.resolveQueryParameters(
                        queryParameters,
                        jsonArguments,
                    ) shouldBe ResolvedQueryParameters(emptyMap())
                }
            }

            describe("exceptions") {

                it("should throw exception if parameter is required and argument is not provided") {

                    val jsonArguments =
                        buildJsonObject {
                            put(commentIdParameterName, "456")
                        }

                    val queryParameters =
                        listOf(
                            createPrimitiveParameter(itemIdParameterName, isRequired = true),
                        )

                    shouldThrow<Exception> {
                        resolver.resolveQueryParameters(
                            queryParameters,
                            jsonArguments,
                        )
                    }
                }

                it("should throw exception if parameter is required and argument is null") {

                    val jsonArguments =
                        buildJsonObject {
                            put(itemIdParameterName, JsonNull)
                        }

                    val queryParameters =
                        listOf(
                            createPrimitiveParameter(itemIdParameterName, isRequired = true),
                        )

                    shouldThrow<Exception> {
                        resolver.resolveQueryParameters(
                            queryParameters,
                            jsonArguments,
                        )
                    }
                }
            }
        }
    })
