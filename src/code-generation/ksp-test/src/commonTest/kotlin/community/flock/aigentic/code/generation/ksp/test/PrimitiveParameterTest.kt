package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.tool.ParameterRegistry
import community.flock.aigentic.core.tool.getParameter
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.generated.parameter.PersonParameter
import community.flock.aigentic.generated.parameter.initialize
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PrimitiveParameterTest {

    @BeforeTest
    fun setup() {
        ParameterRegistry.initialize()
    }

    private fun getPrimitiveParameter(objectParam: Parameter.Complex.Object, name: String): Parameter.Primitive {
        val param = objectParam.parameters.find { it.name == name }
        assertNotNull(param, "$name parameter should exist")
        assertTrue(param is Parameter.Primitive, "$name parameter should be a Primitive")
        return param
    }

    @Test
    fun testPersonIdParameterShouldHaveCorrectDescription() {
        val personParam = PersonParameter.parameter
        val idParam = getPrimitiveParameter(personParam, "id")
        assertEquals("Unique identifier for the person", idParam.description, "Id parameter should have correct description")
    }

    @Test
    fun testPersonNameParameterShouldHaveCorrectDescription() {
        val personParam = PersonParameter.parameter
        val nameParam = getPrimitiveParameter(personParam, "name")
        assertEquals("Full name of the person", nameParam.description, "Name parameter should have correct description")
    }

    @Test
    fun testPersonAgeParameterShouldHaveCorrectDescription() {
        val personParam = PersonParameter.parameter
        val ageParam = getPrimitiveParameter(personParam, "age")
        assertEquals("Age of the person in years", ageParam.description, "Age parameter should have correct description")
    }

    @Test
    fun testStringPropertiesShouldBeMappedToStringType() {
        val personParam = PersonParameter.parameter
        val idParam = getPrimitiveParameter(personParam, "id")
        assertEquals(ParameterType.Primitive.String, idParam.type, "String should be mapped to String type")
    }

    @Test
    fun testIntegerPropertiesShouldBeMappedToIntegerType() {
        val personParam = PersonParameter.parameter
        val ageParam = getPrimitiveParameter(personParam, "age")
        assertEquals(ParameterType.Primitive.Integer, ageParam.type, "Int should be mapped to Integer type")
    }

    @Test
    fun testNonNullablePropertiesShouldBeMarkedAsRequired() {
        val personParam = PersonParameter.parameter
        val idParam = getPrimitiveParameter(personParam, "id")
        assertTrue(idParam.isRequired, "Non-nullable property should be marked as required")
    }

    @Test
    fun testNullablePropertiesShouldBeMarkedAsNotRequired() {
        val personParam = PersonParameter.parameter
        val nameParam = getPrimitiveParameter(personParam, "name")
        assertFalse(nameParam.isRequired, "Nullable property should be marked as not required")
    }

    @Test
    fun testAllPropertiesInAllNullableShouldBeMarkedAsNotRequired() {
        val allNullableParam = getParameter<AllNullable>() as? Parameter.Complex.Object
        assertNotNull(allNullableParam, "AllNullable parameter should exist")

        allNullableParam.parameters.forEach { param ->
            assertFalse(param.isRequired, "All properties in AllNullable should be marked as not required")
        }
    }

    @Test
    fun testDataClassWithSinglePropertyShouldBeProcessedCorrectly() {
        val emptyLikeParam = getParameter<EmptyLike>() as Parameter.Complex.Object
        assertEquals(1, emptyLikeParam.parameters.size, "EmptyLike should have 1 parameter")

        val dummyParam = getPrimitiveParameter(emptyLikeParam, "dummy")
        assertEquals("dummy", dummyParam.name, "Parameter name should be 'dummy'")
        assertEquals(ParameterType.Primitive.String, dummyParam.type, "Dummy should be mapped to String type")
    }

    @Test
    fun testFloatPropertiesShouldBeMappedToNumberType() {
        val numberTypesParam = getParameter<NumberTypes>() as Parameter.Complex.Object
        val floatParam = getPrimitiveParameter(numberTypesParam, "floatValue")
        assertEquals(ParameterType.Primitive.Number, floatParam.type, "Float should be mapped to Number type")
    }

    @Test
    fun testDoublePropertiesShouldBeMappedToNumberType() {
        val numberTypesParam = getParameter<NumberTypes>() as Parameter.Complex.Object
        val doubleParam = getPrimitiveParameter(numberTypesParam, "doubleValue")
        assertEquals(ParameterType.Primitive.Number, doubleParam.type, "Double should be mapped to Number type")
    }

    @Test
    fun testNullableFloatPropertiesShouldBeMappedToNumberTypeAndMarkedAsNotRequired() {
        val numberTypesParam = getParameter<NumberTypes>() as Parameter.Complex.Object
        val nullableFloatParam = getPrimitiveParameter(numberTypesParam, "nullableFloat")
        assertEquals(ParameterType.Primitive.Number, nullableFloatParam.type, "Nullable Float should be mapped to Number type")
        assertFalse(nullableFloatParam.isRequired, "Nullable Float should be marked as not required")
    }

    @Test
    fun testBooleanPropertiesShouldBeMappedToBooleanType() {
        val booleanTypesParam = getParameter<BooleanTypes>() as Parameter.Complex.Object
        val booleanParam = getPrimitiveParameter(booleanTypesParam, "booleanValue")
        assertEquals(ParameterType.Primitive.Boolean, booleanParam.type, "Boolean should be mapped to Boolean type")
        assertTrue(booleanParam.isRequired, "Non-nullable Boolean should be marked as required")
    }

    @Test
    fun testNullableBooleanPropertiesShouldBeMappedToBooleanTypeAndMarkedAsNotRequired() {
        val booleanTypesParam = getParameter<BooleanTypes>() as Parameter.Complex.Object
        val nullableBooleanParam = getPrimitiveParameter(booleanTypesParam, "nullableBoolean")
        assertEquals(ParameterType.Primitive.Boolean, nullableBooleanParam.type, "Nullable Boolean should be mapped to Boolean type")
        assertFalse(nullableBooleanParam.isRequired, "Nullable Boolean should be marked as not required")
    }

    @Test
    fun testStringValueShouldBeMappedToStringType() {
        val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
        val stringParam = getPrimitiveParameter(allPrimitiveTypesParam, "stringValue")
        assertEquals(ParameterType.Primitive.String, stringParam.type, "String should be mapped to String type")
    }

    @Test
    fun testIntValueShouldBeMappedToIntegerType() {
        val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
        val intParam = getPrimitiveParameter(allPrimitiveTypesParam, "intValue")
        assertEquals(ParameterType.Primitive.Integer, intParam.type, "Int should be mapped to Integer type")
    }

    @Test
    fun testLongValueShouldBeMappedToIntegerType() {
        val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
        val longParam = getPrimitiveParameter(allPrimitiveTypesParam, "longValue")
        assertEquals(ParameterType.Primitive.Integer, longParam.type, "Long should be mapped to Integer type")
    }

    @Test
    fun testFloatValueShouldBeMappedToNumberType() {
        val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
        val floatParam = getPrimitiveParameter(allPrimitiveTypesParam, "floatValue")
        assertEquals(ParameterType.Primitive.Number, floatParam.type, "Float should be mapped to Number type")
    }

    @Test
    fun testDoubleValueShouldBeMappedToNumberType() {
        val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
        val doubleParam = getPrimitiveParameter(allPrimitiveTypesParam, "doubleValue")
        assertEquals(ParameterType.Primitive.Number, doubleParam.type, "Double should be mapped to Number type")
    }

    @Test
    fun testBooleanValueShouldBeMappedToBooleanType() {
        val allPrimitiveTypesParam = getParameter<AllPrimitiveTypes>() as Parameter.Complex.Object
        val booleanParam = getPrimitiveParameter(allPrimitiveTypesParam, "booleanValue")
        assertEquals(ParameterType.Primitive.Boolean, booleanParam.type, "Boolean should be mapped to Boolean type")
    }
}
