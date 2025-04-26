package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterRegistry
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import community.flock.aigentic.core.tool.getParameter
import community.flock.aigentic.generated.parameter.initialize
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beInstanceOf
import io.kotest.matchers.types.shouldBeInstanceOf

class ArrayParameterTest : DescribeSpec({

    beforeTest {
        ParameterRegistry.initialize()
    }

    describe("Array Parameter Tests") {

        describe("Description Tests") {
            it("should have correct description for id parameter") {
                val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
                val idParam = taggedItemParam.parameters.find { it.name == "id" } as? Parameter.Primitive

                idParam shouldNotBe null
                idParam!!.description shouldBe "Unique identifier for the tagged item"
            }

            it("should have correct description for name parameter") {
                val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
                val nameParam = taggedItemParam.parameters.find { it.name == "name" } as? Parameter.Primitive

                nameParam shouldNotBe null
                nameParam!!.description shouldBe "Display name of the item"
            }

            it("should have correct description for tags parameter") {
                val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
                val tagsParam = taggedItemParam.parameters.find { it.name == "tags" } as? Parameter.Complex.Array

                tagsParam shouldNotBe null
                tagsParam!!.description shouldBe "List of tags associated with this item"
            }
        }

        describe("Complex Array Tests") {
            it("should have employees parameter as a Complex.Array") {
                val companyParam = getParameter<Company>() as Parameter.Complex.Object
                val employeesParam = companyParam.parameters.find { it.name == "employees" }

                employeesParam shouldNotBe null
                employeesParam.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("should have employees item definition as a Complex.Object") {
                val companyParam = getParameter<Company>() as Parameter.Complex.Object
                val employeesParam = companyParam.parameters.find { it.name == "employees" } as Parameter.Complex.Array

                employeesParam.itemDefinition.shouldBeInstanceOf<Parameter.Complex.Object>()
            }

            it("should have correct properties for employee object") {
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
            it("should have tags parameter as a Complex.Array") {
                val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
                val tagsParam = taggedItemParam.parameters.find { it.name == "tags" }

                tagsParam shouldNotBe null
                tagsParam.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("should have tags item definition as a Primitive") {
                val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
                val tagsParam = taggedItemParam.parameters.find { it.name == "tags" } as Parameter.Complex.Array

                tagsParam.itemDefinition.shouldBeInstanceOf<Parameter.Primitive>()
            }

            it("should have String type for tags item") {
                val taggedItemParam = getParameter<TaggedItem>() as Parameter.Complex.Object
                val tagsParam = taggedItemParam.parameters.find { it.name == "tags" } as Parameter.Complex.Array
                val tagDefinition = tagsParam.itemDefinition as Parameter.Primitive

                tagDefinition.type shouldBe ParameterType.Primitive.String
            }
        }

        describe("Nullable Item Array Tests") {
            it("should have items parameter as a Complex.Array") {
                val nullableItemListParam = getParameter<NullableItemList>() as Parameter.Complex.Object
                val itemsParam = nullableItemListParam.parameters.find { it.name == "items" }

                itemsParam shouldNotBe null
                itemsParam.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("should have items item definition as a Primitive") {
                val nullableItemListParam = getParameter<NullableItemList>() as Parameter.Complex.Object
                val itemsParam = nullableItemListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array

                itemsParam.itemDefinition.shouldBeInstanceOf<Parameter.Primitive>()
            }

            it("should treat items as required by processor") {
                val nullableItemListParam = getParameter<NullableItemList>() as Parameter.Complex.Object
                val itemsParam = nullableItemListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array
                val itemDefinition = itemsParam.itemDefinition as Parameter.Primitive

                itemDefinition.type shouldBe ParameterType.Primitive.String
                itemDefinition.isRequired shouldBe true
            }
        }

        describe("Nullable Array Tests") {
            it("should have nullable array items parameter as a Complex.Array") {
                val nullableArrayListParam = getParameter<NullableArrayList>() as Parameter.Complex.Object
                val itemsParam = nullableArrayListParam.parameters.find { it.name == "items" }

                itemsParam shouldNotBe null
                itemsParam.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("should have items parameter as nullable") {
                val nullableArrayListParam = getParameter<NullableArrayList>() as Parameter.Complex.Object
                val itemsParam = nullableArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array

                itemsParam.isRequired shouldBe false
            }

            it("should have nullable array items item definition as a Primitive") {
                val nullableArrayListParam = getParameter<NullableArrayList>() as Parameter.Complex.Object
                val itemsParam = nullableArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array

                itemsParam.itemDefinition.shouldBeInstanceOf<Parameter.Primitive>()
            }

            it("should have items item as non-nullable") {
                val nullableArrayListParam = getParameter<NullableArrayList>() as Parameter.Complex.Object
                val itemsParam = nullableArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array
                val itemDefinition = itemsParam.itemDefinition as Parameter.Primitive

                itemDefinition.type shouldBe ParameterType.Primitive.String
                itemDefinition.isRequired shouldBe true
            }
        }

        describe("Nested Array Tests") {
            it("should have nested array items parameter as a Complex.Array") {
                val nestedArrayListParam = getParameter<NestedArrayList>() as Parameter.Complex.Object
                val itemsParam = nestedArrayListParam.parameters.find { it.name == "items" }

                itemsParam shouldNotBe null
                itemsParam.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("should have items item definition as a Complex.Array") {
                val nestedArrayListParam = getParameter<NestedArrayList>() as Parameter.Complex.Object
                val outerArray = nestedArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array

                outerArray.itemDefinition.shouldBeInstanceOf<Parameter.Complex.Array>()
            }

            it("should have inner array item definition as a Primitive") {
                val nestedArrayListParam = getParameter<NestedArrayList>() as Parameter.Complex.Object
                val outerArray = nestedArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array
                val innerArray = outerArray.itemDefinition as Parameter.Complex.Array

                innerArray.itemDefinition.shouldBeInstanceOf<Parameter.Primitive>()
            }

            it("should have String type for inner array item") {
                val nestedArrayListParam = getParameter<NestedArrayList>() as Parameter.Complex.Object
                val outerArray = nestedArrayListParam.parameters.find { it.name == "items" } as Parameter.Complex.Array
                val innerArray = outerArray.itemDefinition as Parameter.Complex.Array
                val itemDefinition = innerArray.itemDefinition as Parameter.Primitive

                itemDefinition.type shouldBe ParameterType.Primitive.String
            }
        }

        describe("Enum Array Tests") {
            it("should have statuses parameter as a Complex.Array") {
                val enumArrayListParam = getParameter<EnumArrayList>() as Parameter.Complex.Object
                val statusesParam = enumArrayListParam.parameters.find { it.name == "statuses" }

                statusesParam shouldNotBe null
                statusesParam should beInstanceOf<Parameter.Complex.Array>()
            }

            it("should have statuses item definition as a Complex.Enum") {
                val enumArrayListParam = getParameter<EnumArrayList>() as Parameter.Complex.Object
                val statusesArray = enumArrayListParam.parameters.find { it.name == "statuses" } as Parameter.Complex.Array

                statusesArray.itemDefinition.shouldBeInstanceOf<Parameter.Complex.Enum>()
            }

            it("should have correct value type for enum definition") {
                val enumArrayListParam = getParameter<EnumArrayList>() as Parameter.Complex.Object
                val statusesArray = enumArrayListParam.parameters.find { it.name == "statuses" } as Parameter.Complex.Array
                val enumDefinition = statusesArray.itemDefinition as Parameter.Complex.Enum

                enumDefinition.valueType shouldBe ParameterType.Primitive.String
            }

            it("should have correct values for enum definition") {
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
