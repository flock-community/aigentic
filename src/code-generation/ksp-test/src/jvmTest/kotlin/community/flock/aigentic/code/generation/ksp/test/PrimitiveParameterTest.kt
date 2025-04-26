package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterRegistry
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.getParameter
import community.flock.aigentic.generated.parameter.PersonParameter
import community.flock.aigentic.generated.parameter.initialize
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class PrimitiveParameterTest : DescribeSpec({

    beforeTest {
        ParameterRegistry.initialize()
    }

    fun getPrimitiveParameter(
        objectParam: Parameter.Complex.Object,
        name: String,
    ): Parameter.Primitive {
        val param = objectParam.parameters.find { it.name == name }
        param shouldNotBe null
        param.shouldBeInstanceOf<Parameter.Primitive>()
        return param
    }

    describe("Primitive Parameter Tests") {

        describe("Description Tests") {
            it("should have correct description for person id parameter") {
                val personParam = PersonParameter.parameter
                val idParam = getPrimitiveParameter(personParam, "id")
                idParam.description shouldBe "Unique identifier for the person"
            }

            it("should have correct description for person name parameter") {
                val personParam = PersonParameter.parameter
                val nameParam = getPrimitiveParameter(personParam, "name")
                nameParam.description shouldBe "Full name of the person"
            }

            it("should have correct description for person age parameter") {
                val personParam = PersonParameter.parameter
                val ageParam = getPrimitiveParameter(personParam, "age")
                ageParam.description shouldBe "Age of the person in years"
            }
        }

        describe("Type Mapping Tests") {
            it("should map string properties to String type") {
                val personParam = PersonParameter.parameter
                val idParam = getPrimitiveParameter(personParam, "id")
                idParam.type shouldBe ParameterType.Primitive.String
            }

            it("should map integer properties to Integer type") {
                val personParam = PersonParameter.parameter
                val ageParam = getPrimitiveParameter(personParam, "age")
                ageParam.type shouldBe ParameterType.Primitive.Integer
            }
        }

        describe("Nullability Tests") {
            it("should mark non-nullable properties as required") {
                val personParam = PersonParameter.parameter
                val idParam = getPrimitiveParameter(personParam, "id")
                idParam.isRequired shouldBe true
            }

            it("should mark nullable properties as not required") {
                val personParam = PersonParameter.parameter
                val nameParam = getPrimitiveParameter(personParam, "name")
                nameParam.isRequired shouldBe false
            }

            it("should mark all properties in AllNullable as not required") {
                val allNullableParam = getParameter<AllNullable>() as Parameter.Complex.Object
                allNullableParam shouldNotBe null

                allNullableParam.parameters.forEach { param ->
                    param.isRequired shouldBe false
                }
            }
        }

        describe("Special Cases Tests") {
            it("should process data class with single property correctly") {
                val emptyLikeParam = getParameter<EmptyLike>() as Parameter.Complex.Object
                emptyLikeParam.parameters.size shouldBe 1

                val dummyParam = getPrimitiveParameter(emptyLikeParam, "dummy")
                dummyParam.name shouldBe "dummy"
                dummyParam.type shouldBe ParameterType.Primitive.String
            }
        }

        describe("Number Type Tests") {
            it("should map float properties to Number type") {
                val numberTypesParam = getParameter<NumberTypes>() as Parameter.Complex.Object
                val floatParam = getPrimitiveParameter(numberTypesParam, "floatValue")
                floatParam.type shouldBe ParameterType.Primitive.Number
            }

            it("should map double properties to Number type") {
                val numberTypesParam = getParameter<NumberTypes>() as Parameter.Complex.Object
                val doubleParam = getPrimitiveParameter(numberTypesParam, "doubleValue")
                doubleParam.type shouldBe ParameterType.Primitive.Number
            }

            it("should map nullable float properties to Number type and mark as not required") {
                val numberTypesParam = getParameter<NumberTypes>() as Parameter.Complex.Object
                val nullableFloatParam = getPrimitiveParameter(numberTypesParam, "nullableFloat")
                nullableFloatParam.type shouldBe ParameterType.Primitive.Number
                nullableFloatParam.isRequired shouldBe false
            }
        }

        describe("Boolean Type Tests") {
            it("should map boolean properties to Boolean type") {
                val booleanTypesParam = getParameter<BooleanTypes>() as Parameter.Complex.Object
                val booleanParam = getPrimitiveParameter(booleanTypesParam, "booleanValue")
                booleanParam.type shouldBe ParameterType.Primitive.Boolean
                booleanParam.isRequired shouldBe true
            }

            it("should map nullable boolean properties to Boolean type and mark as not required") {
                val booleanTypesParam = getParameter<BooleanTypes>() as Parameter.Complex.Object
                val nullableBooleanParam = getPrimitiveParameter(booleanTypesParam, "nullableBoolean")
                nullableBooleanParam.type shouldBe ParameterType.Primitive.Boolean
                nullableBooleanParam.isRequired shouldBe false
            }
        }

        describe("All Primitive Types Tests") {
            it("should map string value to String type") {
                val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
                val stringParam = getPrimitiveParameter(allPrimitiveTypesParam, "stringValue")
                stringParam.type shouldBe ParameterType.Primitive.String
            }

            it("should map int value to Integer type") {
                val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
                val intParam = getPrimitiveParameter(allPrimitiveTypesParam, "intValue")
                intParam.type shouldBe ParameterType.Primitive.Integer
            }

            it("should map long value to Integer type") {
                val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
                val longParam = getPrimitiveParameter(allPrimitiveTypesParam, "longValue")
                longParam.type shouldBe ParameterType.Primitive.Integer
            }

            it("should map float value to Number type") {
                val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
                val floatParam = getPrimitiveParameter(allPrimitiveTypesParam, "floatValue")
                floatParam.type shouldBe ParameterType.Primitive.Number
            }

            it("should map double value to Number type") {
                val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
                val doubleParam = getPrimitiveParameter(allPrimitiveTypesParam, "doubleValue")
                doubleParam.type shouldBe ParameterType.Primitive.Number
            }

            it("should map boolean value to Boolean type") {
                val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
                val booleanParam = getPrimitiveParameter(allPrimitiveTypesParam, "booleanValue")
                booleanParam.type shouldBe ParameterType.Primitive.Boolean
            }
        }
    }
})
