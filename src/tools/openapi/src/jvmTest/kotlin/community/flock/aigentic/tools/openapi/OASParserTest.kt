@file:Suppress("ktlint:standard:max-line-length")

package community.flock.aigentic.tools.openapi

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import community.flock.aigentic.tools.http.EndpointOperation
import community.flock.aigentic.tools.http.EndpointOperation.Method.GET
import community.flock.aigentic.tools.http.EndpointOperation.Method.POST
import community.flock.aigentic.tools.http.EndpointOperation.Method.PUT
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class OASParserTest : DescribeSpec({

    describe("PrimitiveParameter") {

        it("should parse integer path params") {

            val operation = "/path-param-integer.json".parseOperations().first { it.name == "getItemById" }

            operation shouldBe
                EndpointOperation(
                    name = "getItemById", description = "Get an item by its ID", method = GET,
                    pathParams =
                        listOf(
                            Parameter.Primitive(
                                name = "itemId",
                                description = "The ID of the item to retrieve",
                                isRequired = true,
                                type = ParameterType.Primitive.Integer,
                            ),
                        ),
                    url = "https://example.com/api/item/{itemId}", queryParams = emptyList(), requestBody = null,
                )
        }

        it("should parse string query param") {

            val operation = "/query-param-string.json".parseOperations().first { it.name == "getItemsByName" }

            operation shouldBe
                EndpointOperation(
                    name = "getItemsByName",
                    description = "Get items by name",
                    method = GET,
                    pathParams = emptyList(),
                    queryParams =
                        listOf(
                            Parameter.Primitive(
                                name = "itemName",
                                description = "The name of the item to search for",
                                isRequired = false,
                                type = ParameterType.Primitive.String,
                            ),
                        ),
                    url = "https://example.com/api/items",
                    requestBody = null,
                )
        }
    }

    describe("ArrayParameter") {

        it("should parse array query params") {

            val operation = "/query-param-string-array.json".parseOperations().first { it.name == "getItemsByIds" }

            operation shouldBe
                EndpointOperation(
                    name = "getItemsByIds",
                    description = null,
                    method = GET,
                    pathParams = emptyList(),
                    url = "https://example.com/api/items",
                    queryParams =
                        listOf(
                            Parameter.Complex.Array(
                                name = "itemIds",
                                description = "The IDs of the items to retrieve",
                                isRequired = false,
                                itemDefinition =
                                    Parameter.Primitive(
                                        name = "item",
                                        description = null,
                                        isRequired = false,
                                        type = ParameterType.Primitive.Integer,
                                    ),
                            ),
                        ),
                    requestBody = null,
                )
        }

        describe("should parse request body which has a array property which is of type object") {

            val operations = "/request-body-object-array.json".parseOperations()
            operations.size shouldBe 1
            operations.first() shouldBe
                EndpointOperation(
                    name = "POST /users",
                    description = "Creates a new user with the provided information, including multiple addresses.",
                    method = POST,
                    pathParams = emptyList(),
                    url = "https://example.com/api/users",
                    queryParams = emptyList(),
                    requestBody =
                        Parameter.Complex.Object(
                            name = "body", description = "User to be created", isRequired = true,
                            parameters =
                                listOf(
                                    Parameter.Primitive(
                                        name = "username",
                                        description = null,
                                        isRequired = true,
                                        type = ParameterType.Primitive.String,
                                    ),
                                    Parameter.Complex.Array(
                                        "addresses",
                                        description = null,
                                        isRequired = true,
                                        itemDefinition =
                                            Parameter.Complex.Object(
                                                "item", description = null, true,
                                                parameters =
                                                    listOf(
                                                        Parameter.Primitive(
                                                            name = "street",
                                                            description = null,
                                                            isRequired = true,
                                                            type = ParameterType.Primitive.String,
                                                        ),
                                                        Parameter.Primitive(
                                                            name = "city",
                                                            description = null,
                                                            isRequired = true,
                                                            type = ParameterType.Primitive.String,
                                                        ),
                                                        Parameter.Primitive(
                                                            name = "zipcode",
                                                            description = null,
                                                            isRequired = true,
                                                            type = ParameterType.Primitive.String,
                                                        ),
                                                    ),
                                            ),
                                    ),
                                ),
                        ),
                )
        }

        it("should parse request body which has a array property which is of type array") {

            val operations = "/request-body-array-array.json".parseOperations()
            operations.size shouldBe 1
            operations.first() shouldBe
                EndpointOperation(
                    name = "POST /projects",
                    description = "Creates a new project with the provided information, including team members.",
                    method = POST,
                    pathParams = emptyList(),
                    url = "https://example.com/api/projects",
                    queryParams = emptyList(),
                    requestBody =
                        Parameter.Complex.Object(
                            name = "body", description = "Project to be created", isRequired = true,
                            parameters =
                                listOf(
                                    Parameter.Primitive(
                                        name = "projectName",
                                        description = null,
                                        isRequired = true,
                                        type = ParameterType.Primitive.String,
                                    ),
                                    Parameter.Complex.Array(
                                        "teamMembers",
                                        description = null,
                                        isRequired = true,
                                        itemDefinition =
                                            Parameter.Complex.Array(
                                                "item", description = null, false,
                                                itemDefinition =
                                                    Parameter.Complex.Object(
                                                        name = "item", description = null, isRequired = false,
                                                        parameters =
                                                            listOf(
                                                                Parameter.Primitive(
                                                                    name = "name",
                                                                    description = null,
                                                                    isRequired = false,
                                                                    type = ParameterType.Primitive.String,
                                                                ),
                                                                Parameter.Primitive(
                                                                    name = "role",
                                                                    description = null,
                                                                    isRequired = false,
                                                                    type = ParameterType.Primitive.String,
                                                                ),
                                                            ),
                                                    ),
                                            ),
                                    ),
                                ),
                        ),
                )
        }

        it("should parse request body which has a array property which is of type primitive enum") {
            val operations = "/request-body-primitive-enum-array.json".parseOperations()
            operations.size shouldBe 1
            operations.first() shouldBe
                EndpointOperation(
                    name = "POST /tasks",
                    description = "Updates the statuses of tasks based on the provided information.",
                    method = POST,
                    pathParams = emptyList(),
                    url = "https://example.com/api/tasks",
                    queryParams = emptyList(),
                    requestBody =
                        Parameter.Complex.Object(
                            name = "body",
                            description = "List of task statuses to be updated",
                            isRequired = true,
                            parameters =
                                listOf(
                                    Parameter.Primitive(
                                        name = "taskId",
                                        description = null,
                                        isRequired = true,
                                        type = ParameterType.Primitive.String,
                                    ),
                                    Parameter.Complex.Array(
                                        "statuses",
                                        description = null,
                                        isRequired = true,
                                        itemDefinition =
                                            Parameter.Complex.Enum(
                                                name = "item", description = null, isRequired = true, default = null,
                                                values =
                                                    listOf(
                                                        PrimitiveValue.String("Not Started"),
                                                        PrimitiveValue.String("In Progress"),
                                                        PrimitiveValue.String("Completed"),
                                                        PrimitiveValue.String("Blocked"),
                                                    ),
                                                valueType = ParameterType.Primitive.String,
                                            ),
                                    ),
                                ),
                        ),
                )
        }
    }

    describe("EnumParameter") {

        it("should parse string enum query params") {

            val operation = "/query-param-string-enum.json".parseOperations().first { it.name == "getItemsByCategory" }

            operation shouldBe
                EndpointOperation(
                    name = "getItemsByCategory",
                    description = null,
                    method = GET,
                    pathParams = emptyList(),
                    url = "https://example.com/api/items",
                    queryParams =
                        listOf(
                            Parameter.Complex.Enum(
                                name = "category",
                                description = "The category of the items to retrieve",
                                isRequired = true,
                                default = PrimitiveValue.String("clothing"),
                                values =
                                    listOf(
                                        PrimitiveValue.String("electronics"),
                                        PrimitiveValue.String("clothing"),
                                        PrimitiveValue.String("furniture"),
                                    ),
                                valueType = ParameterType.Primitive.String,
                            ),
                        ),
                    requestBody = null,
                )
        }

        it("should parse request body which has an integer enum property") {

            val operations = "/request-body-integer-enum.json".parseOperations()
            operations.size shouldBe 1
            operations.first() shouldBe
                EndpointOperation(
                    name = "POST /submitStatus",
                    description = "Submits a status code represented as an integer.",
                    method = POST,
                    pathParams = emptyList(),
                    url = "https://example.com/api/submitStatus",
                    queryParams = emptyList(),
                    requestBody =
                        Parameter.Complex.Object(
                            name = "body",
                            description = "Payload containing a status code",
                            isRequired = true,
                            parameters =
                                listOf(
                                    Parameter.Complex.Enum(
                                        name = "statusCode",
                                        description = "Status code representing the state of an item. 0: New, 1: In Progress, 2: Completed, 3: Archived.",
                                        isRequired = true,
                                        default = null,
                                        values =
                                            listOf(
                                                PrimitiveValue.Integer(0),
                                                PrimitiveValue.Integer(1),
                                                PrimitiveValue.Integer(2),
                                                PrimitiveValue.Integer(3),
                                            ),
                                        valueType = ParameterType.Primitive.Integer,
                                    ),
                                ),
                        ),
                )
        }
    }

    describe("ObjectParameter") {

        it("should map object request body params") {

            val operation = "/request-body-object.json".parseOperations().first { it.name == "createItem" }

            operation shouldBe
                EndpointOperation(
                    name = "createItem",
                    description = "Create an item",
                    method = PUT,
                    pathParams = emptyList(),
                    url = "https://example.com/api/item",
                    queryParams = emptyList(),
                    requestBody =
                        Parameter.Complex.Object(
                            name = "body", description = "Item to be created",
                            parameters =
                                listOf(
                                    Parameter.Primitive(
                                        name = "name", description = null, isRequired = true, type = ParameterType.Primitive.String,
                                    ),
                                    Parameter.Primitive(
                                        name = "description",
                                        description = null,
                                        isRequired = false,
                                        type = ParameterType.Primitive.String,
                                    ),
                                    Parameter.Primitive(
                                        name = "price", description = null, isRequired = true, type = ParameterType.Primitive.Number,
                                    ),
                                ),
                            isRequired = true,
                        ),
                )
        }
    }

    context("should parse all operations") {
        withData(
            "/petstore.json" to 19,
            "/github.json" to 930,
            "/hackernews.json" to 7,
        ) { (specFile, expectedNumberOfOperations) ->
            specFile.parseOperations().size shouldBe expectedNumberOfOperations
        }
    }
})

private fun String.parseOperations(): List<EndpointOperation> {
    val json = OASParserTest::class.java.getResource(this)!!.readText(Charsets.UTF_8)
    return OpenAPIv3Parser.parseOperations(json)
}
