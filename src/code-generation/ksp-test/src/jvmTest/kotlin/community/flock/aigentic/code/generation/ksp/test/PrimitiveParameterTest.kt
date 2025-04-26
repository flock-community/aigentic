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

class PrimitiveParameterTest : DescribeSpec({

    beforeTest {
        ParameterRegistry.initialize()
    }

    describe("Primitive Parameter Tests") {

        describe("Description Tests") {
            it("Person id parameter should have correct description") {
                val personParam = PersonParameter.parameter
                val idParam = getPrimitiveParameter(personParam, "id")
                idParam.description shouldBe "Unique identifier for the person"
            }

            it("Person name parameter should have correct description") {
                val personParam = PersonParameter.parameter
                val nameParam = getPrimitiveParameter(personParam, "name")
                nameParam.description shouldBe "Full name of the person"
            }

            it("Person age parameter should have correct description") {
                val personParam = PersonParameter.parameter
                val ageParam = getPrimitiveParameter(personParam, "age")
                ageParam.description shouldBe "Age of the person in years"
            }
        }

        describe("Type Mapping Tests") {
            it("String properties should be mapped to String type") {
                val personParam = PersonParameter.parameter
                val idParam = getPrimitiveParameter(personParam, "id")
                idParam.type shouldBe ParameterType.Primitive.String
            }

            it("Integer properties should be mapped to Integer type") {
                val personParam = PersonParameter.parameter
                val ageParam = getPrimitiveParameter(personParam, "age")
                ageParam.type shouldBe ParameterType.Primitive.Integer
            }
        }

        describe("Nullability Tests") {
            it("Non-nullable properties should be marked as required") {
                val personParam = PersonParameter.parameter
                val idParam = getPrimitiveParameter(personParam, "id")
                idParam.isRequired shouldBe true
            }

            it("Nullable properties should be marked as not required") {
                val personParam = PersonParameter.parameter
                val nameParam = getPrimitiveParameter(personParam, "name")
                nameParam.isRequired shouldBe false
            }

            it("All properties in AllNullable should be marked as not required") {
                val allNullableParam = getParameter<AllNullable>() as Parameter.Complex.Object
                allNullableParam shouldNotBe null

                allNullableParam.parameters.forEach { param ->
                    param.isRequired shouldBe false
                }
            }
        }

        describe("Special Cases Tests") {
            it("Data class with single property should be processed correctly") {
                val emptyLikeParam = getParameter<EmptyLike>() as Parameter.Complex.Object
                emptyLikeParam.parameters.size shouldBe 1

                val dummyParam = getPrimitiveParameter(emptyLikeParam, "dummy")
                dummyParam.name shouldBe "dummy"
                dummyParam.type shouldBe ParameterType.Primitive.String
            }
        }

        describe("Number Type Tests") {
            it("Float properties should be mapped to Number type") {
                val numberTypesParam = getParameter<NumberTypes>() as Parameter.Complex.Object
                val floatParam = getPrimitiveParameter(numberTypesParam, "floatValue")
                floatParam.type shouldBe ParameterType.Primitive.Number
            }

            it("Double properties should be mapped to Number type") {
                val numberTypesParam = getParameter<NumberTypes>() as Parameter.Complex.Object
                val doubleParam = getPrimitiveParameter(numberTypesParam, "doubleValue")
                doubleParam.type shouldBe ParameterType.Primitive.Number
            }

            it("Nullable float properties should be mapped to Number type and marked as not required") {
                val numberTypesParam = getParameter<NumberTypes>() as Parameter.Complex.Object
                val nullableFloatParam = getPrimitiveParameter(numberTypesParam, "nullableFloat")
                nullableFloatParam.type shouldBe ParameterType.Primitive.Number
                nullableFloatParam.isRequired shouldBe false
            }
        }

        describe("Boolean Type Tests") {
            it("Boolean properties should be mapped to Boolean type") {
                val booleanTypesParam = getParameter<BooleanTypes>() as Parameter.Complex.Object
                val booleanParam = getPrimitiveParameter(booleanTypesParam, "booleanValue")
                booleanParam.type shouldBe ParameterType.Primitive.Boolean
                booleanParam.isRequired shouldBe true
            }

            it("Nullable boolean properties should be mapped to Boolean type and marked as not required") {
                val booleanTypesParam = getParameter<BooleanTypes>() as Parameter.Complex.Object
                val nullableBooleanParam = getPrimitiveParameter(booleanTypesParam, "nullableBoolean")
                nullableBooleanParam.type shouldBe ParameterType.Primitive.Boolean
                nullableBooleanParam.isRequired shouldBe false
            }
        }

        describe("All Primitive Types Tests") {
            it("String value should be mapped to String type") {
                val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
                val stringParam = getPrimitiveParameter(allPrimitiveTypesParam, "stringValue")
                stringParam.type shouldBe ParameterType.Primitive.String
            }

            it("Int value should be mapped to Integer type") {
                val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
                val intParam = getPrimitiveParameter(allPrimitiveTypesParam, "intValue")
                intParam.type shouldBe ParameterType.Primitive.Integer
            }

            it("Long value should be mapped to Integer type") {
                val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
                val longParam = getPrimitiveParameter(allPrimitiveTypesParam, "longValue")
                longParam.type shouldBe ParameterType.Primitive.Integer
            }

            it("Float value should be mapped to Number type") {
                val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
                val floatParam = getPrimitiveParameter(allPrimitiveTypesParam, "floatValue")
                floatParam.type shouldBe ParameterType.Primitive.Number
            }

            it("Double value should be mapped to Number type") {
                val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
                val doubleParam = getPrimitiveParameter(allPrimitiveTypesParam, "doubleValue")
                doubleParam.type shouldBe ParameterType.Primitive.Number
            }

            it("Boolean value should be mapped to Boolean type") {
                val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
                val booleanParam = getPrimitiveParameter(allPrimitiveTypesParam, "booleanValue")
                booleanParam.type shouldBe ParameterType.Primitive.Boolean
            }
        }
    }
})
