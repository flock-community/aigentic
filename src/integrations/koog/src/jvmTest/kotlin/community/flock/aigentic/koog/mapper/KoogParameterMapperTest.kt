package community.flock.aigentic.koog.mapper

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class KoogParameterMapperTest :
    DescribeSpec({

        describe("ToolParameterType mapping") {

            it("maps primitive types") {
                ToolParameterDescriptor("city", "The city", ToolParameterType.String)
                    .toAigenticParameter(isRequired = true) shouldBe
                    Parameter.Primitive("city", "The city", true, ParameterType.Primitive.String)

                ToolParameterDescriptor("count", "The count", ToolParameterType.Integer)
                    .toAigenticParameter(isRequired = false) shouldBe
                    Parameter.Primitive("count", "The count", false, ParameterType.Primitive.Integer)
            }

            it("maps enum types") {
                val parameter =
                    ToolParameterDescriptor("unit", "Temperature unit", ToolParameterType.Enum(arrayOf("CELSIUS", "FAHRENHEIT")))
                        .toAigenticParameter(isRequired = true)

                parameter.shouldBeInstanceOf<Parameter.Complex.Enum>().run {
                    values shouldBe listOf(PrimitiveValue.String("CELSIUS"), PrimitiveValue.String("FAHRENHEIT"))
                    valueType shouldBe ParameterType.Primitive.String
                }
            }

            it("maps list types recursively") {
                val parameter =
                    ToolParameterDescriptor("cities", "Cities to check", ToolParameterType.List(ToolParameterType.String))
                        .toAigenticParameter(isRequired = true)

                parameter.shouldBeInstanceOf<Parameter.Complex.Array>().run {
                    itemDefinition.shouldBeInstanceOf<Parameter.Primitive>().run {
                        type shouldBe ParameterType.Primitive.String
                    }
                }
            }

            it("maps object types with required/optional properties") {
                val objectType =
                    ToolParameterType.Object(
                        properties =
                            listOf(
                                ToolParameterDescriptor("city", "City name", ToolParameterType.String),
                                ToolParameterDescriptor("country", "Country name", ToolParameterType.String),
                            ),
                        requiredProperties = listOf("city"),
                    )

                val parameter = ToolParameterDescriptor("location", "Location", objectType).toAigenticParameter(isRequired = true)

                parameter.shouldBeInstanceOf<Parameter.Complex.Object>().run {
                    parameters.first { it.name == "city" }.isRequired shouldBe true
                    parameters.first { it.name == "country" }.isRequired shouldBe false
                }
            }
        }

        describe("ToolDescriptor mapping") {

            it("splits required and optional parameters and preserves name/description") {
                val descriptor =
                    ToolDescriptor(
                        name = "get_weather",
                        description = "Returns the current weather for a city",
                        requiredParameters = listOf(ToolParameterDescriptor("city", "The city", ToolParameterType.String)),
                        optionalParameters = listOf(ToolParameterDescriptor("unit", "Temperature unit", ToolParameterType.String)),
                    )

                val tool = descriptor.toAigenticTool()

                tool.name.value shouldBe "get_weather"
                tool.description shouldBe "Returns the current weather for a city"
                tool.parameters.first { it.name == "city" }.isRequired shouldBe true
                tool.parameters.first { it.name == "unit" }.isRequired shouldBe false
            }
        }
    })
