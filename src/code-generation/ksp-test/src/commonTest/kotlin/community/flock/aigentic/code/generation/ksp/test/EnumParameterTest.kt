package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.tool.ParameterRegistry
import community.flock.aigentic.core.tool.getParameter
import community.flock.aigentic.generated.parameter.initialize
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EnumParameterTest {

    @BeforeTest
    fun setup() {
        ParameterRegistry.initialize()
    }

    // Helper methods
    private fun getEnumParameter(objectParam: Parameter.Complex.Object, name: String): Parameter.Complex.Enum {
        val param = objectParam.parameters.find { it.name == name }
        assertNotNull(param, "$name parameter should exist")
        assertTrue(param is Parameter.Complex.Enum, "$name parameter should be a Complex.Enum")
        return param
    }

    private fun assertEnumHasCorrectValues(enumParam: Parameter.Complex.Enum, expectedValues: List<String>) {
        assertEquals(expectedValues.size, enumParam.values.size, "Enum parameter should have ${expectedValues.size} values")

        for (value in expectedValues) {
            val enumValue = enumParam.values.find { it is PrimitiveValue.String && it.value == value }
            assertNotNull(enumValue, "Enum parameter should have $value value")
        }
    }

    // Description Tests
    @Test
    fun testStatusParameterShouldHaveCorrectDescription() {
        val todoParam = getParameter<Todo>() as Parameter.Complex.Object
        val enumParam = getEnumParameter(todoParam, "status")
        assertEquals("Current status of the todo item", enumParam.description, "Status parameter should have correct description")
    }

    // Basic Enum Tests
    @Test
    fun testEnumParameterShouldHaveCorrectName() {
        val todoParam = getParameter<Todo>() as Parameter.Complex.Object
        val enumParam = getEnumParameter(todoParam, "status")
        assertEquals("status", enumParam.name, "Enum parameter should have name 'status'")
    }

    @Test
    fun testEnumParameterShouldBeRequiredByDefault() {
        val todoParam = getParameter<Todo>() as Parameter.Complex.Object
        val enumParam = getEnumParameter(todoParam, "status")
        assertTrue(enumParam.isRequired, "Enum parameter should be required")
    }

    @Test
    fun testEnumParameterShouldHaveStringValueType() {
        val todoParam = getParameter<Todo>() as Parameter.Complex.Object
        val enumParam = getEnumParameter(todoParam, "status")
        assertEquals(ParameterType.Primitive.String, enumParam.valueType, "Enum parameter should have String valueType")
    }

    @Test
    fun testEnumParameterShouldHaveCorrectValues() {
        val todoParam = getParameter<Todo>() as Parameter.Complex.Object
        val enumParam = getEnumParameter(todoParam, "status")
        assertEnumHasCorrectValues(enumParam, listOf("COMPLETED", "IN_PROGRESS"))
    }

    // Nullable Enum Tests
    @Test
    fun testNullableEnumParameterShouldNotBeRequired() {
        val nullableEnumParam = getParameter<NullableEnumProperty>() as Parameter.Complex.Object
        val enumParam = getEnumParameter(nullableEnumParam, "status")
        assertFalse(enumParam.isRequired, "Nullable enum parameter should not be required")
    }

    @Test
    fun testNullableEnumParameterShouldHaveCorrectValues() {
        val nullableEnumParam = getParameter<NullableEnumProperty>() as Parameter.Complex.Object
        val enumParam = getEnumParameter(nullableEnumParam, "status")
        assertEnumHasCorrectValues(enumParam, listOf("COMPLETED", "IN_PROGRESS"))
    }

    // Default Value Enum Tests
    @Test
    fun testEnumParameterWithDefaultValueShouldBeRequired() {
        val defaultEnumParam = getParameter<DefaultEnumProperty>() as Parameter.Complex.Object
        val enumParam = getEnumParameter(defaultEnumParam, "status")
        assertTrue(enumParam.isRequired, "Enum parameter with default value should be required")
    }

    @Test
    fun testEnumParameterWithDefaultValueShouldHaveCorrectValues() {
        val defaultEnumParam = getParameter<DefaultEnumProperty>() as Parameter.Complex.Object
        val enumParam = getEnumParameter(defaultEnumParam, "status")
        assertEnumHasCorrectValues(enumParam, listOf("COMPLETED", "IN_PROGRESS"))
    }

    // Multiple Enum Tests
    @Test
    fun testClassWithMultipleEnumPropertiesShouldHaveCorrectParameterNames() {
        val multipleEnumParam = getParameter<MultipleEnumProperties>() as Parameter.Complex.Object
        val primaryEnumParam = getEnumParameter(multipleEnumParam, "primaryStatus")
        val secondaryEnumParam = getEnumParameter(multipleEnumParam, "secondaryStatus")
        assertEquals("primaryStatus", primaryEnumParam.name, "Primary enum parameter should have name 'primaryStatus'")
        assertEquals("secondaryStatus", secondaryEnumParam.name, "Secondary enum parameter should have name 'secondaryStatus'")
    }

    @Test
    fun testMultipleEnumParametersShouldBothBeRequired() {
        val multipleEnumParam = getParameter<MultipleEnumProperties>() as Parameter.Complex.Object
        val primaryEnumParam = getEnumParameter(multipleEnumParam, "primaryStatus")
        val secondaryEnumParam = getEnumParameter(multipleEnumParam, "secondaryStatus")
        assertTrue(primaryEnumParam.isRequired, "Primary enum parameter should be required")
        assertTrue(secondaryEnumParam.isRequired, "Secondary enum parameter should be required")
    }

    @Test
    fun testMultipleEnumParametersShouldHaveStringValueType() {
        val multipleEnumParam = getParameter<MultipleEnumProperties>() as Parameter.Complex.Object
        val primaryEnumParam = getEnumParameter(multipleEnumParam, "primaryStatus")
        val secondaryEnumParam = getEnumParameter(multipleEnumParam, "secondaryStatus")
        assertEquals(ParameterType.Primitive.String, primaryEnumParam.valueType, "Primary enum parameter should have String valueType")
        assertEquals(ParameterType.Primitive.String, secondaryEnumParam.valueType, "Secondary enum parameter should have String valueType")
    }

    @Test
    fun testMultipleEnumParametersShouldHaveCorrectValues() {
        val multipleEnumParam = getParameter<MultipleEnumProperties>() as Parameter.Complex.Object
        val primaryEnumParam = getEnumParameter(multipleEnumParam, "primaryStatus")
        val secondaryEnumParam = getEnumParameter(multipleEnumParam, "secondaryStatus")
        assertEnumHasCorrectValues(primaryEnumParam, listOf("COMPLETED", "IN_PROGRESS"))
        assertEnumHasCorrectValues(secondaryEnumParam, listOf("COMPLETED", "IN_PROGRESS"))
    }

    // Nested Enum Tests
    @Test
    fun testNestedObjectShouldContainEnumParameter() {
        val nestedEnumParam = getParameter<NestedEnumProperty>() as Parameter.Complex.Object
        val taskParam = nestedEnumParam.parameters.find { it.name == "task" }
        assertNotNull(taskParam, "Task parameter should exist")
        assertTrue(taskParam is Parameter.Complex.Object, "Task parameter should be a Complex.Object")
    }

    @Test
    fun testEnumParameterInNestedObjectShouldHaveCorrectProperties() {
        val nestedEnumParam = getParameter<NestedEnumProperty>() as Parameter.Complex.Object
        val taskObject = nestedEnumParam.parameters.find { it.name == "task" } as Parameter.Complex.Object
        val enumParam = getEnumParameter(taskObject, "status")
        assertEquals("status", enumParam.name, "Enum parameter should have name 'status'")
        assertTrue(enumParam.isRequired, "Enum parameter should be required")
        assertEquals(ParameterType.Primitive.String, enumParam.valueType, "Enum parameter should have String valueType")
    }

    @Test
    fun testEnumParameterInNestedObjectShouldHaveCorrectValues() {
        val nestedEnumParam = getParameter<NestedEnumProperty>() as Parameter.Complex.Object
        val taskObject = nestedEnumParam.parameters.find { it.name == "task" } as Parameter.Complex.Object
        val enumParam = getEnumParameter(taskObject, "status")
        assertEnumHasCorrectValues(enumParam, listOf("COMPLETED", "IN_PROGRESS"))
    }
}
