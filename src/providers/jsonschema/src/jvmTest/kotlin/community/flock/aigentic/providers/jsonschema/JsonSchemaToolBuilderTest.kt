package community.flock.aigentic.providers.jsonschema

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class JsonSchemaToolBuilderTest :
    DescribeSpec({

        it("should build properties and required") {
            val parameters =
                listOf(
                    Parameter.Complex.Object(
                        name = "person",
                        description = "description",
                        isRequired = true,
                        parameters =
                            listOf(
                                Parameter.Primitive(
                                    name = "name",
                                    description = "description",
                                    type = ParameterType.Primitive.String,
                                    isRequired = true,
                                ),
                                Parameter.Primitive(
                                    name = "age",
                                    description = "description",
                                    type = ParameterType.Primitive.Integer,
                                    isRequired = true,
                                ),
                            ),
                    ),
                    Parameter.Complex.Enum(
                        name = "type",
                        description = "description",
                        isRequired = false,
                        default = PrimitiveValue.String("A"),
                        values = listOf(PrimitiveValue.String("A"), PrimitiveValue.String("B"), PrimitiveValue.String("C")),
                        valueType = ParameterType.Primitive.String,
                    ),
                )

            buildJsonObject {
                emitPropertiesAndRequired(parameters, SchemaDialect.OPENAPI_SCHEMA)
            } shouldBe
                buildJsonObject {
                    putJsonObject("properties") {
                        putJsonObject("person") {
                            put("type", "object")
                            put("description", "description")
                            putJsonObject("properties") {
                                putJsonObject("name") {
                                    put("type", "string")
                                    put("description", "description")
                                }
                                putJsonObject("age") {
                                    put("type", "integer")
                                    put("description", "description")
                                }
                            }
                            putJsonArray("required") {
                                add("name")
                                add("age")
                            }
                        }
                        putJsonObject("type") {
                            put("type", "string")
                            put("nullable", true)
                            put("description", "description")
                            putJsonArray("enum") {
                                add("A")
                                add("B")
                                add("C")
                            }
                        }
                    }
                    putJsonArray("required") {
                        add("person")
                    }
                }
        }

        describe("optional parameters") {

            val parameters =
                listOf(
                    Parameter.Primitive(
                        name = "name",
                        description = null,
                        isRequired = true,
                        type = ParameterType.Primitive.String,
                    ),
                    Parameter.Primitive(
                        name = "age",
                        description = null,
                        isRequired = false,
                        type = ParameterType.Primitive.Integer,
                    ),
                    Parameter.Complex.Enum(
                        name = "unit",
                        description = null,
                        isRequired = false,
                        default = PrimitiveValue.String("MM"),
                        values = listOf(PrimitiveValue.String("MM"), PrimitiveValue.String("CM")),
                        valueType = ParameterType.Primitive.String,
                    ),
                    Parameter.Complex.Object(
                        name = "size",
                        description = null,
                        isRequired = false,
                        parameters =
                            listOf(
                                Parameter.Primitive(
                                    name = "value",
                                    description = null,
                                    isRequired = true,
                                    type = ParameterType.Primitive.Number,
                                ),
                            ),
                    ),
                    Parameter.Complex.Array(
                        name = "tags",
                        description = null,
                        isRequired = false,
                        itemDefinition =
                            Parameter.Primitive(
                                name = "Item",
                                description = null,
                                isRequired = false,
                                type = ParameterType.Primitive.String,
                            ),
                    ),
                )

            it("should mark optional parameters nullable and leave them out of required for the OpenAPI schema dialect") {
                buildJsonObject {
                    emitPropertiesAndRequired(parameters, SchemaDialect.OPENAPI_SCHEMA)
                } shouldBe
                    Json.parseToJsonElement(
                        """
                        {
                          "properties": {
                            "name": { "type": "string" },
                            "age": { "type": "integer", "nullable": true },
                            "unit": { "type": "string", "nullable": true, "enum": ["MM", "CM"] },
                            "size": {
                              "type": "object",
                              "nullable": true,
                              "properties": { "value": { "type": "number" } },
                              "required": ["value"]
                            },
                            "tags": { "type": "array", "nullable": true, "items": { "type": "string", "nullable": true } }
                          },
                          "required": ["name"]
                        }
                        """,
                    )
            }

            it("should add null to the type of optional parameters and leave them out of required for the JSON schema dialect") {
                buildJsonObject {
                    emitPropertiesAndRequired(parameters, SchemaDialect.JSON_SCHEMA)
                } shouldBe
                    Json.parseToJsonElement(
                        """
                        {
                          "properties": {
                            "name": { "type": "string" },
                            "age": { "type": ["integer", "null"] },
                            "unit": { "type": ["string", "null"], "enum": ["MM", "CM", null] },
                            "size": {
                              "type": ["object", "null"],
                              "properties": { "value": { "type": "number" } },
                              "required": ["value"]
                            },
                            "tags": { "type": ["array", "null"], "items": { "type": ["string", "null"] } }
                          },
                          "required": ["name"]
                        }
                        """,
                    )
            }

            it("should add null to the type of optional parameters and list every parameter in required for the OpenAI JSON schema dialect") {
                buildJsonObject {
                    emitPropertiesAndRequired(parameters, SchemaDialect.OPENAI_JSON_SCHEMA)
                } shouldBe
                    Json.parseToJsonElement(
                        """
                        {
                          "properties": {
                            "name": { "type": "string" },
                            "age": { "type": ["integer", "null"] },
                            "unit": { "type": ["string", "null"], "enum": ["MM", "CM", null] },
                            "size": {
                              "type": ["object", "null"],
                              "properties": { "value": { "type": "number" } },
                              "required": ["value"]
                            },
                            "tags": { "type": ["array", "null"], "items": { "type": ["string", "null"] } }
                          },
                          "required": ["name", "age", "unit", "size", "tags"]
                        }
                        """,
                    )
            }
        }
    })
