package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.Aigentic
import community.flock.aigentic.core.tool.Parameter
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
        Aigentic.initialize()
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

    fun getEnumParameter(
        objectParam: Parameter.Complex.Object,
        name: String,
    ): Parameter.Complex.Enum {
        val param = objectParam.parameters.find { it.name == name }
        param shouldNotBe null
        param.shouldBeInstanceOf<Parameter.Complex.Enum>()
        return param
    }

    describe("Enum Parameters") {

        describe("Basic Properties") {
            it("should have correct description") {
                val todoParam = getParameter<Todo>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(todoParam, "status")
                enumParam.description shouldBe "Current status of the todo item"
            }

            it("should have correct name") {
                val todoParam = getParameter<Todo>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(todoParam, "status")
                enumParam.name shouldBe "status"
            }

            it("should be required by default") {
                val todoParam = getParameter<Todo>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(todoParam, "status")
                enumParam.isRequired shouldBe true
            }

            it("should have String valueType") {
                val todoParam = getParameter<Todo>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(todoParam, "status")
                enumParam.valueType shouldBe ParameterType.Primitive.String
            }

            it("should have correct values") {
                val todoParam = getParameter<Todo>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(todoParam, "status")
                assertEnumHasCorrectValues(enumParam, listOf("COMPLETED", "IN_PROGRESS"))
            }
        }

        describe("Nullable Enum") {
            it("should not be required") {
                val nullableEnumParam = getParameter<NullableEnumProperty>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(nullableEnumParam, "status")
                enumParam.isRequired shouldBe false
            }

            it("should have correct values") {
                val nullableEnumParam = getParameter<NullableEnumProperty>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(nullableEnumParam, "status")
                assertEnumHasCorrectValues(enumParam, listOf("COMPLETED", "IN_PROGRESS"))
            }
        }

        describe("Enum with Default Value") {
            it("should be required") {
                val defaultEnumParam = getParameter<DefaultEnumProperty>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(defaultEnumParam, "status")
                enumParam.isRequired shouldBe true
            }

            it("should have correct values") {
                val defaultEnumParam = getParameter<DefaultEnumProperty>() as Parameter.Complex.Object
                val enumParam = getEnumParameter(defaultEnumParam, "status")
                assertEnumHasCorrectValues(enumParam, listOf("COMPLETED", "IN_PROGRESS"))
            }
        }

        describe("Multiple Enums") {
            it("should have correct parameter names") {
                val multipleEnumParam = getParameter<MultipleEnumProperties>() as Parameter.Complex.Object
                val primaryEnumParam = getEnumParameter(multipleEnumParam, "primaryStatus")
                val secondaryEnumParam = getEnumParameter(multipleEnumParam, "secondaryStatus")
                primaryEnumParam.name shouldBe "primaryStatus"
                secondaryEnumParam.name shouldBe "secondaryStatus"
            }

            it("should both be required") {
                val multipleEnumParam = getParameter<MultipleEnumProperties>() as Parameter.Complex.Object
                val primaryEnumParam = getEnumParameter(multipleEnumParam, "primaryStatus")
                val secondaryEnumParam = getEnumParameter(multipleEnumParam, "secondaryStatus")
                primaryEnumParam.isRequired shouldBe true
                secondaryEnumParam.isRequired shouldBe true
            }

            it("should have String valueType") {
                val multipleEnumParam = getParameter<MultipleEnumProperties>() as Parameter.Complex.Object
                val primaryEnumParam = getEnumParameter(multipleEnumParam, "primaryStatus")
                val secondaryEnumParam = getEnumParameter(multipleEnumParam, "secondaryStatus")
                primaryEnumParam.valueType shouldBe ParameterType.Primitive.String
                secondaryEnumParam.valueType shouldBe ParameterType.Primitive.String
            }

            it("should have correct values") {
                val multipleEnumParam = getParameter<MultipleEnumProperties>() as Parameter.Complex.Object
                val primaryEnumParam = getEnumParameter(multipleEnumParam, "primaryStatus")
                val secondaryEnumParam = getEnumParameter(multipleEnumParam, "secondaryStatus")
                assertEnumHasCorrectValues(primaryEnumParam, listOf("COMPLETED", "IN_PROGRESS"))
                assertEnumHasCorrectValues(secondaryEnumParam, listOf("COMPLETED", "IN_PROGRESS"))
            }
        }

        describe("Nested Enum") {
            it("should be contained in nested object") {
                val nestedEnumParam = getParameter<NestedEnumProperty>() as Parameter.Complex.Object
                val taskParam = nestedEnumParam.parameters.find { it.name == "task" }
                taskParam shouldNotBe null
                taskParam.shouldBeInstanceOf<Parameter.Complex.Object>()
            }

            it("should have correct properties") {
                val nestedEnumParam = getParameter<NestedEnumProperty>() as Parameter.Complex.Object
                val taskObject = nestedEnumParam.parameters.find { it.name == "task" } as Parameter.Complex.Object
                val enumParam = getEnumParameter(taskObject, "status")
                enumParam.name shouldBe "status"
                enumParam.isRequired shouldBe true
                enumParam.valueType shouldBe ParameterType.Primitive.String
            }

            it("should have correct values") {
                val nestedEnumParam = getParameter<NestedEnumProperty>() as Parameter.Complex.Object
                val taskObject = nestedEnumParam.parameters.find { it.name == "task" } as Parameter.Complex.Object
                val enumParam = getEnumParameter(taskObject, "status")
                assertEnumHasCorrectValues(enumParam, listOf("COMPLETED", "IN_PROGRESS"))
            }
        }
    }
})
