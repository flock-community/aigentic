package community.flock.aigentic.tools.jsonschema

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class ToolMapperKtTest : DescribeSpec({

    it("should emit properties and required") {
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
            emitPropertiesAndRequired(parameters)
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
})
