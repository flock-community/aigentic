package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.tool.getParameter
import community.flock.aigentic.generated.parameter.initialize
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterRegistry
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ArrayParameterTest {

    @BeforeTest
    fun setup() {
        ParameterRegistry.initialize()
    }

    // Description Tests
    @Test
    fun testIdParameterShouldHaveCorrectDescription() {
        val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
        val idParam = taggedItemParam.parameters.find { it.name == "id" } as? Parameter.Primitive

        assertNotNull(idParam, "Id parameter should exist")
        assertEquals("Unique identifier for the tagged item", idParam.description, "Id parameter should have correct description")
    }

    @Test
    fun testNameParameterShouldHaveCorrectDescription() {
        val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
        val nameParam = taggedItemParam.parameters.find { it.name == "name" } as? Parameter.Primitive

        assertNotNull(nameParam, "Name parameter should exist")
        assertEquals("Display name of the item", nameParam.description, "Name parameter should have correct description")
    }

    @Test
    fun testTagsParameterShouldHaveCorrectDescription() {
        val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
        val tagsParam = taggedItemParam.parameters.find { it.name == "tags" } as? Parameter.Complex.Array

        assertNotNull(tagsParam, "Tags parameter should exist")
        assertEquals("List of tags associated with this item", tagsParam.description, "Tags parameter should have correct description")
    }

    // Complex Array Tests
    @Test
    fun testEmployeesParameterShouldBeAComplexArray() {
        val companyParam = getParameter<Company>() as Parameter.Complex.Object
        val employeesParam = companyParam.parameters.find { it.name == "employees" }

        assertNotNull(employeesParam, "Employees parameter should exist")
        assertTrue(employeesParam is Parameter.Complex.Array, "Employees parameter should be a Complex.Array")
    }

    @Test
    fun testEmployeesItemDefinitionShouldBeAComplexObject() {
        val companyParam = getParameter<Company>() as Parameter.Complex.Object
        val employeesParam = companyParam.parameters.find { it.name == "employees" } as Parameter.Complex.Array

        assertTrue(
            employeesParam.itemDefinition is Parameter.Complex.Object,
            "Item definition should be a Complex.Object"
        )
    }

    @Test
    fun testEmployeeObjectShouldHaveCorrectProperties() {
        val companyParam = getParameter<Company>() as Parameter.Complex.Object
        val employeesParam = companyParam.parameters.find { it.name == "employees" } as Parameter.Complex.Array
        val employeeDefinition = employeesParam.itemDefinition as Parameter.Complex.Object

        assertEquals(2, employeeDefinition.parameters.size, "Employee should have 2 properties")

        val nameParam = employeeDefinition.parameters.find { it.name == "name" } as? Parameter.Primitive
        val positionParam = employeeDefinition.parameters.find { it.name == "position" } as? Parameter.Primitive

        assertNotNull(nameParam, "Name parameter should exist")
        assertNotNull(positionParam, "Position parameter should exist")

        assertEquals(ParameterType.Primitive.String, nameParam.type, "Name should be mapped to String type")
        assertEquals(ParameterType.Primitive.String, positionParam.type, "Position should be mapped to String type")
    }

    // Primitive Array Tests
    @Test
    fun testTagsParameterShouldBeAComplexArray() {
        val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
        val tagsParam = taggedItemParam.parameters.find { it.name == "tags" }

        assertNotNull(tagsParam, "Tags parameter should exist")
        assertTrue(tagsParam is Parameter.Complex.Array, "Tags parameter should be a Complex.Array")
    }

    @Test
    fun testTagsItemDefinitionShouldBeAPrimitive() {
        val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
        val tagsParam = taggedItemParam.parameters.find { it.name == "tags" } as Parameter.Complex.Array

        assertTrue(
            tagsParam.itemDefinition is Parameter.Primitive,
            "Item definition should be a Primitive"
        )
    }

    @Test
    fun testTagsItemShouldHaveStringType() {
        val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
        val tagsParam = taggedItemParam.parameters.find { it.name == "tags" } as Parameter.Complex.Array
        val tagDefinition = tagsParam.itemDefinition as Parameter.Primitive

        assertEquals(ParameterType.Primitive.String, tagDefinition.type, "Tag should be mapped to String type")
    }

    // Nullable Item Array Tests
    @Test
    fun testItemsParameterShouldBeAComplexArray() {
        val nullableItemListParam = getParameter<NullableItemList>() as Parameter.Complex.Object
        val itemsParam = nullableItemListParam.parameters.find { it.name == "items" }

        assertNotNull(itemsParam, "Items parameter should exist")
        assertTrue(itemsParam is Parameter.Complex.Array, "Items parameter should be a Complex.Array")
    }

    @Test
    fun testItemsItemDefinitionShouldBeAPrimitive() {
        val nullableItemListParam = getParameter<NullableItemList>() as Parameter.Complex.Object
        val itemsParam = nullableItemListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array

        assertTrue(
            itemsParam.itemDefinition is Parameter.Primitive,
            "Item definition should be a Primitive"
        )
    }

    @Test
    fun testItemsShouldBeTreatedAsRequiredByProcessor() {
        val nullableItemListParam = getParameter<NullableItemList>() as Parameter.Complex.Object
        val itemsParam = nullableItemListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array
        val itemDefinition = itemsParam.itemDefinition as Parameter.Primitive

        assertEquals(ParameterType.Primitive.String, itemDefinition.type, "Item should be mapped to String type")
        assertTrue(itemDefinition.isRequired, "Item is treated as required by the processor")
    }

    // Nullable Array Tests
    @Test
    fun testNullableArrayItemsParameterShouldBeAComplexArray() {
        val nullableArrayListParam = getParameter<NullableArrayList>() as Parameter.Complex.Object
        val itemsParam = nullableArrayListParam.parameters.find { it.name == "items" }

        assertNotNull(itemsParam, "Items parameter should exist")
        assertTrue(itemsParam is Parameter.Complex.Array, "Items parameter should be a Complex.Array")
    }

    @Test
    fun testItemsParameterShouldBeNullable() {
        val nullableArrayListParam = getParameter<NullableArrayList>() as Parameter.Complex.Object
        val itemsParam = nullableArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array

        assertFalse(itemsParam.isRequired, "Array parameter should not be required (nullable)")
    }

    @Test
    fun testNullableArrayItemsItemDefinitionShouldBeAPrimitive() {
        val nullableArrayListParam = getParameter<NullableArrayList>() as Parameter.Complex.Object
        val itemsParam = nullableArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array

        assertTrue(
            itemsParam.itemDefinition is Parameter.Primitive,
            "Item definition should be a Primitive"
        )
    }

    @Test
    fun testItemsItemShouldBeNonNullable() {
        val nullableArrayListParam = getParameter<NullableArrayList>() as Parameter.Complex.Object
        val itemsParam = nullableArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array
        val itemDefinition = itemsParam.itemDefinition as Parameter.Primitive

        assertEquals(ParameterType.Primitive.String, itemDefinition.type, "Item should be mapped to String type")
        assertTrue(itemDefinition.isRequired, "Item should be required (non-nullable)")
    }

    // Nested Array Tests
    @Test
    fun testNestedArrayItemsParameterShouldBeAComplexArray() {
        val nestedArrayListParam = getParameter<NestedArrayList>() as Parameter.Complex.Object
        val itemsParam = nestedArrayListParam.parameters.find { it.name == "items" }

        assertNotNull(itemsParam, "Items parameter should exist")
        assertTrue(itemsParam is Parameter.Complex.Array, "Items parameter should be a Complex.Array")
    }

    @Test
    fun testItemsItemDefinitionShouldBeAComplexArray() {
        val nestedArrayListParam = getParameter<NestedArrayList>() as Parameter.Complex.Object
        val outerArray = nestedArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array

        assertTrue(
            outerArray.itemDefinition is Parameter.Complex.Array,
            "Item definition should be a Complex.Array"
        )
    }

    @Test
    fun testInnerArrayItemDefinitionShouldBeAPrimitive() {
        val nestedArrayListParam = getParameter<NestedArrayList>() as Parameter.Complex.Object
        val outerArray = nestedArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array
        val innerArray = outerArray.itemDefinition as Parameter.Complex.Array

        assertTrue(
            innerArray.itemDefinition is Parameter.Primitive,
            "Inner array's item definition should be a Primitive"
        )
    }

    @Test
    fun testInnerArrayItemShouldHaveStringType() {
        val nestedArrayListParam = getParameter<NestedArrayList>() as Parameter.Complex.Object
        val outerArray = nestedArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array
        val innerArray = outerArray.itemDefinition as Parameter.Complex.Array
        val itemDefinition = innerArray.itemDefinition as Parameter.Primitive

        assertEquals(ParameterType.Primitive.String, itemDefinition.type, "Item should be mapped to String type")
    }

    // Enum Array Tests
    @Test
    fun testStatusesParameterShouldBeAComplexArray() {
        val enumArrayListParam = getParameter<EnumArrayList>() as Parameter.Complex.Object
        val statusesParam = enumArrayListParam.parameters.find { it.name == "statuses" }

        assertNotNull(statusesParam, "Statuses parameter should exist")
        assertTrue(statusesParam is Parameter.Complex.Array, "Statuses parameter should be a Complex.Array")
    }

    @Test
    fun testStatusesItemDefinitionShouldBeAComplexEnum() {
        val enumArrayListParam = getParameter<EnumArrayList>() as Parameter.Complex.Object
        val statusesArray = enumArrayListParam.parameters.find { it.name == "statuses" } as Parameter.Complex.Array

        assertTrue(
            statusesArray.itemDefinition is Parameter.Complex.Enum,
            "Item definition should be a Complex.Enum"
        )
    }

    @Test
    fun testEnumDefinitionShouldHaveCorrectValueType() {
        val enumArrayListParam = getParameter<EnumArrayList>() as Parameter.Complex.Object
        val statusesArray = enumArrayListParam.parameters.find { it.name == "statuses" } as Parameter.Complex.Array
        val enumDefinition = statusesArray.itemDefinition as Parameter.Complex.Enum

        assertEquals(ParameterType.Primitive.String, enumDefinition.valueType, "Enum should have String valueType")
    }

    @Test
    fun testEnumDefinitionShouldHaveCorrectValues() {
        val enumArrayListParam = getParameter<EnumArrayList>() as Parameter.Complex.Object
        val statusesArray = enumArrayListParam.parameters.find { it.name == "statuses" } as Parameter.Complex.Array
        val enumDefinition = statusesArray.itemDefinition as Parameter.Complex.Enum

        assertEquals(2, enumDefinition.values.size, "Enum should have 2 values")

        val completedValue = enumDefinition.values.find { it is PrimitiveValue.String && it.value == "COMPLETED" }
        val inProgressValue = enumDefinition.values.find { it is PrimitiveValue.String && it.value == "IN_PROGRESS" }

        assertNotNull(completedValue, "Enum should have COMPLETED value")
        assertNotNull(inProgressValue, "Enum should have IN_PROGRESS value")
    }
}
