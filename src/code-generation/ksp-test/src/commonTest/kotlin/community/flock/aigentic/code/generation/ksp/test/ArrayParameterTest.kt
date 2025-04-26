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

class ArrayParameterTest : DescribeSpec({

    beforeTest {
        ParameterRegistry.initialize()
    }

    describe("Array Parameter Tests") {

        describe("Description Tests") {
            it("Id parameter should have correct description") {
                val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
                val idParam = taggedItemParam.parameters.find { it.name == "id" } as? Parameter.Primitive

                idParam shouldNotBe null
                idParam!!.description shouldBe "Unique identifier for the tagged item"
            }

            it("Name parameter should have correct description") {
                val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
                val nameParam = taggedItemParam.parameters.find { it.name == "name" } as? Parameter.Primitive

                nameParam shouldNotBe null
                nameParam!!.description shouldBe "Display name of the item"
            }

            it("Tags parameter should have correct description") {
                val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
                val tagsParam = taggedItemParam.parameters.find { it.name == "tags" } as? Parameter.Complex.Array

                tagsParam shouldNotBe null
                tagsParam!!.description shouldBe "List of tags associated with this item"
            }
        }

        describe("Complex Array Tests") {
            it("Employees parameter should be a Complex.Array") {
                val companyParam = getParameter<Company>() as Parameter.Complex.Object
                val employeesParam = companyParam.parameters.find { it.name == "employees" }

                employeesParam shouldNotBe null
                employeesParam.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("Employees item definition should be a Complex.Object") {
                val companyParam = getParameter<Company>() as Parameter.Complex.Object
                val employeesParam = companyParam.parameters.find { it.name == "employees" } as Parameter.Complex.Array

                employeesParam.itemDefinition.shouldBeInstanceOf<Parameter.Complex.Object>()
            }

            it("Employee object should have correct properties") {
                val companyParam = getParameter<Company>() as Parameter.Complex.Object
                val employeesParam = companyParam.parameters.find { it.name == "employees" } as Parameter.Complex.Array
                val employeeDefinition = employeesParam.itemDefinition as Parameter.Complex.Object

                employeeDefinition.parameters.size shouldBe 2

                val nameParam = employeeDefinition.parameters.find { it.name == "name" } as? Parameter.Primitive
                val positionParam = employeeDefinition.parameters.find { it.name == "position" } as? Parameter.Primitive

                nameParam shouldNotBe null
                positionParam shouldNotBe null

                nameParam!!.type shouldBe ParameterType.Primitive.String
                positionParam!!.type shouldBe ParameterType.Primitive.String
            }
        }

        describe("Primitive Array Tests") {
            it("Tags parameter should be a Complex.Array") {
                val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
                val tagsParam = taggedItemParam.parameters.find { it.name == "tags" }

                tagsParam shouldNotBe null
                tagsParam.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("Tags item definition should be a Primitive") {
                val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
                val tagsParam = taggedItemParam.parameters.find { it.name == "tags" } as Parameter.Complex.Array

                tagsParam.itemDefinition.shouldBeInstanceOf<Parameter.Primitive>()
            }

            it("Tags item should have String type") {
                val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
                val tagsParam = taggedItemParam.parameters.find { it.name == "tags" } as Parameter.Complex.Array
                val tagDefinition = tagsParam.itemDefinition as Parameter.Primitive

                tagDefinition.type shouldBe ParameterType.Primitive.String
            }
        }

        describe("Nullable Item Array Tests") {
            it("Items parameter should be a Complex.Array") {
                val nullableItemListParam = getParameter<NullableItemList>() as Parameter.Complex.Object
                val itemsParam = nullableItemListParam.parameters.find { it.name == "items" }

                itemsParam shouldNotBe null
                itemsParam.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("Items item definition should be a Primitive") {
                val nullableItemListParam = getParameter<NullableItemList>() as Parameter.Complex.Object
                val itemsParam = nullableItemListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array

                itemsParam.itemDefinition.shouldBeInstanceOf<Parameter.Primitive>()
            }

            it("Items should be treated as required by processor") {
                val nullableItemListParam = getParameter<NullableItemList>() as Parameter.Complex.Object
                val itemsParam = nullableItemListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array
                val itemDefinition = itemsParam.itemDefinition as Parameter.Primitive

                itemDefinition.type shouldBe ParameterType.Primitive.String
                itemDefinition.isRequired shouldBe true
            }
        }

        describe("Nullable Array Tests") {
            it("Nullable array items parameter should be a Complex.Array") {
                val nullableArrayListParam = getParameter<NullableArrayList>() as Parameter.Complex.Object
                val itemsParam = nullableArrayListParam.parameters.find { it.name == "items" }

                itemsParam shouldNotBe null
                itemsParam.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("Items parameter should be nullable") {
                val nullableArrayListParam = getParameter<NullableArrayList>() as Parameter.Complex.Object
                val itemsParam = nullableArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array

                itemsParam.isRequired shouldBe false
            }

            it("Nullable array items item definition should be a Primitive") {
                val nullableArrayListParam = getParameter<NullableArrayList>() as Parameter.Complex.Object
                val itemsParam = nullableArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array

                itemsParam.itemDefinition.shouldBeInstanceOf<Parameter.Primitive>()
            }

            it("Items item should be non-nullable") {
                val nullableArrayListParam = getParameter<NullableArrayList>() as Parameter.Complex.Object
                val itemsParam = nullableArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array
                val itemDefinition = itemsParam.itemDefinition as Parameter.Primitive

                itemDefinition.type shouldBe ParameterType.Primitive.String
                itemDefinition.isRequired shouldBe true
            }
        }

        describe("Nested Array Tests") {
            it("Nested array items parameter should be a Complex.Array") {
                val nestedArrayListParam = getParameter<NestedArrayList>() as Parameter.Complex.Object
                val itemsParam = nestedArrayListParam.parameters.find { it.name == "items" }

                itemsParam shouldNotBe null
                itemsParam.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("Items item definition should be a Complex.Array") {
                val nestedArrayListParam = getParameter<NestedArrayList>() as Parameter.Complex.Object
                val outerArray = nestedArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array

                outerArray.itemDefinition.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("Inner array item definition should be a Primitive") {
                val nestedArrayListParam = getParameter<NestedArrayList>() as Parameter.Complex.Object
                val outerArray = nestedArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array
                val innerArray = outerArray.itemDefinition as Parameter.Complex.Array

                innerArray.itemDefinition.shouldBeInstanceOf<Parameter.Primitive>()
            }

            it("Inner array item should have String type") {
                val nestedArrayListParam = getParameter<NestedArrayList>() as Parameter.Complex.Object
                val outerArray = nestedArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array
                val innerArray = outerArray.itemDefinition as Parameter.Complex.Array
                val itemDefinition = innerArray.itemDefinition as Parameter.Primitive

                itemDefinition.type shouldBe ParameterType.Primitive.String
            }
        }

        describe("Enum Array Tests") {
            it("Statuses parameter should be a Complex.Array") {
                val enumArrayListParam = getParameter<EnumArrayList>() as Parameter.Complex.Object
                val statusesParam = enumArrayListParam.parameters.find { it.name == "statuses" }

                statusesParam shouldNotBe null
                statusesParam.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("Statuses item definition should be a Complex.Enum") {
                val enumArrayListParam = getParameter<EnumArrayList>() as Parameter.Complex.Object
                val statusesArray = enumArrayListParam.parameters.find { it.name == "statuses" } as Parameter.Complex.Array

                statusesArray.itemDefinition.shouldBeInstanceOf<Parameter.Complex.Enum>()
            }

            it("Enum definition should have correct value type") {
                val enumArrayListParam = getParameter<EnumArrayList>() as Parameter.Complex.Object
                val statusesArray = enumArrayListParam.parameters.find { it.name == "statuses" } as Parameter.Complex.Array
                val enumDefinition = statusesArray.itemDefinition as Parameter.Complex.Enum

                enumDefinition.valueType shouldBe ParameterType.Primitive.String
            }

            it("Enum definition should have correct values") {
                val enumArrayListParam = getParameter<EnumArrayList>() as Parameter.Complex.Object
                val statusesArray = enumArrayListParam.parameters.find { it.name == "statuses" } as Parameter.Complex.Array
                val enumDefinition = statusesArray.itemDefinition as Parameter.Complex.Enum

                enumDefinition.values.size shouldBe 2

                val completedValue = enumDefinition.values.find { it is PrimitiveValue.String && it.value == "COMPLETED" }
                val inProgressValue = enumDefinition.values.find { it is PrimitiveValue.String && it.value == "IN_PROGRESS" }

                completedValue shouldNotBe null
                inProgressValue shouldNotBe null
            }
        }
    }
})
