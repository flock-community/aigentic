package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterRegistry
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import community.flock.aigentic.core.tool.getParameter
import community.flock.aigentic.generated.parameter.initialize
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class EnumParameterTest : DescribeSpec({

    beforeTest {
        ParameterRegistry.initialize()
    }

    fun assertEnumHasCorrectValues(
        enumParam: Parameter.Complex.Enum,
        expectedValues: List<String>,
    ) {
        enumParam.values.size shouldBe expectedValues.size

        for (value in expectedValues) {
            val enumValue = enumParam.values.find { it is PrimitiveValue.String && it.value == value }
            enumValue shouldNotBe null
        }
    }

    describe("Enum Parameter Tests") {

        describe("Description Tests") {
            it("Status parameter should have correct description") {
                val todoParam = getParameter<Todo>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(todoParam, "status")
                enumParam.description shouldBe "Current status of the todo item"
            }
        }

        describe("Basic Enum Tests") {
            it("Enum parameter should have correct name") {
                val todoParam = getParameter<Todo>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(todoParam, "status")
                enumParam.name shouldBe "status"
            }

            it("Enum parameter should be required by default") {
                val todoParam = getParameter<Todo>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(todoParam, "status")
                enumParam.isRequired shouldBe true
            }

            it("Enum parameter should have String valueType") {
                val todoParam = getParameter<Todo>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(todoParam, "status")
                enumParam.valueType shouldBe ParameterType.Primitive.String
            }

            it("Enum parameter should have correct values") {
                val todoParam = getParameter<Todo>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(todoParam, "status")
                assertEnumHasCorrectValues(enumParam, listOf("COMPLETED", "IN_PROGRESS"))
            }
        }

        describe("Nullable Enum Tests") {
            it("Nullable enum parameter should not be required") {
                val nullableEnumParam = getParameter<NullableEnumProperty>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(nullableEnumParam, "status")
                enumParam.isRequired shouldBe false
            }

            it("Nullable enum parameter should have correct values") {
                val nullableEnumParam = getParameter<NullableEnumProperty>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(nullableEnumParam, "status")
                assertEnumHasCorrectValues(enumParam, listOf("COMPLETED", "IN_PROGRESS"))
            }
        }

        describe("Default Value Enum Tests") {
            it("Enum parameter with default value should be required") {
                val defaultEnumParam = getParameter<DefaultEnumProperty>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(defaultEnumParam, "status")
                enumParam.isRequired shouldBe true
            }

            it("Enum parameter with default value should have correct values") {
                val defaultEnumParam = getParameter<DefaultEnumProperty>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(defaultEnumParam, "status")
                assertEnumHasCorrectValues(enumParam, listOf("COMPLETED", "IN_PROGRESS"))
            }
        }

        describe("Multiple Enum Tests") {
            it("Class with multiple enum properties should have correct parameter names") {
                val multipleEnumParam = getParameter<MultipleEnumProperties>() as Parameter.Complex.Object
                val primaryEnumParam = getEnumParameter(multipleEnumParam, "primaryStatus")
                val secondaryEnumParam = getEnumParameter(multipleEnumParam, "secondaryStatus")
                primaryEnumParam.name shouldBe "primaryStatus"
                secondaryEnumParam.name shouldBe "secondaryStatus"
            }

            it("Multiple enum parameters should both be required") {
                val multipleEnumParam = getParameter<MultipleEnumProperties>() as Parameter.Complex.Object
                val primaryEnumParam = getEnumParameter(multipleEnumParam, "primaryStatus")
                val secondaryEnumParam = getEnumParameter(multipleEnumParam, "secondaryStatus")
                primaryEnumParam.isRequired shouldBe true
                secondaryEnumParam.isRequired shouldBe true
            }

            it("Multiple enum parameters should have String valueType") {
                val multipleEnumParam = getParameter<MultipleEnumProperties>() as Parameter.Complex.Object
                val primaryEnumParam = getEnumParameter(multipleEnumParam, "primaryStatus")
                val secondaryEnumParam = getEnumParameter(multipleEnumParam, "secondaryStatus")
                primaryEnumParam.valueType shouldBe ParameterType.Primitive.String
                secondaryEnumParam.valueType shouldBe ParameterType.Primitive.String
            }

            it("Multiple enum parameters should have correct values") {
                val multipleEnumParam = getParameter<MultipleEnumProperties>() as Parameter.Complex.Object
                val primaryEnumParam = getEnumParameter(multipleEnumParam, "primaryStatus")
                val secondaryEnumParam = getEnumParameter(multipleEnumParam, "secondaryStatus")
                assertEnumHasCorrectValues(primaryEnumParam, listOf("COMPLETED", "IN_PROGRESS"))
                assertEnumHasCorrectValues(secondaryEnumParam, listOf("COMPLETED", "IN_PROGRESS"))
            }
        }

        describe("Nested Enum Tests") {
            it("Nested object should contain enum parameter") {
                val nestedEnumParam = getParameter<NestedEnumProperty>() as Parameter.Complex.Object
                val taskParam = nestedEnumParam.parameters.find { it.name == "task" }
                taskParam shouldNotBe null
                taskParam.shouldBeInstanceOf<Parameter.Complex.Object>()
            }

            it("Enum parameter in nested object should have correct properties") {
                val nestedEnumParam = getParameter<NestedEnumProperty>() as Parameter.Complex.Object
                val taskObject = nestedEnumParam.parameters.find { it.name == "task" } as Parameter.Complex.Object
                val enumParam = getEnumParameter(taskObject, "status")
                enumParam.name shouldBe "status"
                enumParam.isRequired shouldBe true
                enumParam.valueType shouldBe ParameterType.Primitive.String
            }

            it("Enum parameter in nested object should have correct values") {
                val nestedEnumParam = getParameter<NestedEnumProperty>() as Parameter.Complex.Object
                val taskObject = nestedEnumParam.parameters.find { it.name == "task" } as Parameter.Complex.Object
                val enumParam = getEnumParameter(taskObject, "status")
                assertEnumHasCorrectValues(enumParam, listOf("COMPLETED", "IN_PROGRESS"))
            }
        }
    }
})
