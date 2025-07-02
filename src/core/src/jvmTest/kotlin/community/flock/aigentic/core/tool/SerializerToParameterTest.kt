package community.flock.aigentic.core.tool

import community.flock.aigentic.core.annotations.Description
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable

@Serializable
@Description("Status enum")
enum class Status {
    ACTIVE,
    INACTIVE,
    PENDING,
}

class SerializerToParameterTest : DescribeSpec({

    describe("SerializerToParameter") {

        it("should convert primitive") {

            val expected =
                Parameter.Primitive(
                    name = "String",
                    description = null,
                    isRequired = true,
                    type = ParameterType.Primitive.String,
                )
            SerializerToParameter.convert<String>() shouldBe expected
        }

        it("should convert basic data class") {
            @Serializable
            data class Answer(
                val answer: String,
            )

            val expected =
                Parameter.Complex.Object(
                    name = "Answer",
                    description = null,
                    isRequired = true,
                    parameters =
                        listOf(
                            Parameter.Primitive(
                                name = "answer",
                                description = null,
                                isRequired = true,
                                type = ParameterType.Primitive.String,
                            ),
                        ),
                )
            SerializerToParameter.convert<Answer>() shouldBe expected
        }

        it("should convert basic data class with description") {
            @Serializable
            @Description("Answer object")
            data class Answer(
                @Description("Answer description")
                val answer: String,
            )

            val expected =
                Parameter.Complex.Object(
                    name = "Answer",
                    description = "Answer object",
                    isRequired = true,
                    parameters =
                        listOf(
                            Parameter.Primitive(
                                name = "answer",
                                description = "Answer description",
                                isRequired = true,
                                type = ParameterType.Primitive.String,
                            ),
                        ),
                )
            SerializerToParameter.convert<Answer>() shouldBe expected
        }

        it("should not convert non annotatd data class") {
            data class Plain(
                val text: String,
            )
            SerializerToParameter.convert<Plain>() shouldBe null
        }

        it("should convert all primitives") {
            @Serializable
            @Description("AllPrimitives object")
            data class AllPrimitives(
                @Description("String description")
                val string: String?,
                @Description("Int description")
                val int: Int,
                @Description("Boolean description")
                val boolean: Boolean?,
                @Description("Number description")
                val number: Double,
            )

            val expected =
                Parameter.Complex.Object(
                    name = "AllPrimitives",
                    description = "AllPrimitives object",
                    isRequired = true,
                    parameters =
                        listOf(
                            Parameter.Primitive(
                                name = "string",
                                description = "String description",
                                isRequired = false,
                                type = ParameterType.Primitive.String,
                            ),
                            Parameter.Primitive(
                                name = "int",
                                description = "Int description",
                                isRequired = true,
                                type = ParameterType.Primitive.Integer,
                            ),
                            Parameter.Primitive(
                                name = "boolean",
                                description = "Boolean description",
                                isRequired = false,
                                type = ParameterType.Primitive.Boolean,
                            ),
                            Parameter.Primitive(
                                name = "number",
                                description = "Number description",
                                isRequired = true,
                                type = ParameterType.Primitive.Number,
                            ),
                        ),
                )
            SerializerToParameter.convert<AllPrimitives>() shouldBe expected
        }

        it("should convert enum class") {

            @Serializable
            @Description("EnumContainer object")
            data class EnumContainer(
                @Description("Status description")
                val status: Status,
            )

            val expected =
                Parameter.Complex.Object(
                    name = "EnumContainer",
                    description = "EnumContainer object",
                    isRequired = true,
                    parameters =
                        listOf(
                            Parameter.Complex.Enum(
                                name = "status",
                                description = "Status description",
                                isRequired = true,
                                default = PrimitiveValue.String("ACTIVE"),
                                values =
                                    listOf(
                                        PrimitiveValue.String("ACTIVE"),
                                        PrimitiveValue.String("INACTIVE"),
                                        PrimitiveValue.String("PENDING"),
                                    ),
                                valueType = ParameterType.Primitive.String,
                            ),
                        ),
                )

            SerializerToParameter.convert<EnumContainer>() shouldBe expected
        }

        it("should convert nested complex objects") {
            @Serializable
            @Description("Address object")
            data class Address(
                @Description("Street name")
                val street: String,
                @Description("City name")
                val city: String,
                @Description("Zip code")
                val zipCode: String?,
            )

            @Serializable
            @Description("Person object")
            data class Person(
                @Description("Person's name")
                val name: String,
                @Description("Person's age")
                val age: Int,
                @Description("Person's address")
                val address: Address,
                @Description("Person's status")
                val status: Status,
            )

            val expected =
                Parameter.Complex.Object(
                    name = "Person",
                    description = "Person object",
                    isRequired = true,
                    parameters =
                        listOf(
                            Parameter.Primitive(
                                name = "name",
                                description = "Person's name",
                                isRequired = true,
                                type = ParameterType.Primitive.String,
                            ),
                            Parameter.Primitive(
                                name = "age",
                                description = "Person's age",
                                isRequired = true,
                                type = ParameterType.Primitive.Integer,
                            ),
                            Parameter.Complex.Object(
                                name = "address",
                                description = "Person's address",
                                isRequired = true,
                                parameters =
                                    listOf(
                                        Parameter.Primitive(
                                            name = "street",
                                            description = "Street name",
                                            isRequired = true,
                                            type = ParameterType.Primitive.String,
                                        ),
                                        Parameter.Primitive(
                                            name = "city",
                                            description = "City name",
                                            isRequired = true,
                                            type = ParameterType.Primitive.String,
                                        ),
                                        Parameter.Primitive(
                                            name = "zipCode",
                                            description = "Zip code",
                                            isRequired = false,
                                            type = ParameterType.Primitive.String,
                                        ),
                                    ),
                            ),
                            Parameter.Complex.Enum(
                                name = "status",
                                description = "Person's status",
                                isRequired = true,
                                default = PrimitiveValue.String("ACTIVE"),
                                values =
                                    listOf(
                                        PrimitiveValue.String("ACTIVE"),
                                        PrimitiveValue.String("INACTIVE"),
                                        PrimitiveValue.String("PENDING"),
                                    ),
                                valueType = ParameterType.Primitive.String,
                            ),
                        ),
                )

            SerializerToParameter.convert<Person>() shouldBe expected
        }

        it("should convert object with list of items") {
            @Serializable
            @Description("Item object")
            data class Item(
                @Description("Item name")
                val name: String,
                @Description("Item quantity")
                val quantity: Int,
            )

            @Serializable
            @Description("ItemList object")
            data class ItemList(
                @Description("List of items")
                val items: List<Item>,
            )

            val expected =
                Parameter.Complex.Object(
                    name = "ItemList",
                    description = "ItemList object",
                    isRequired = true,
                    parameters =
                        listOf(
                            Parameter.Complex.Array(
                                name = "items",
                                description = "List of items",
                                isRequired = true,
                                itemDefinition =
                                    Parameter.Complex.Object(
                                        name = "Item",
                                        description = null,
                                        isRequired = true,
                                        parameters =
                                            listOf(
                                                Parameter.Primitive(
                                                    name = "name",
                                                    description = "Item name",
                                                    isRequired = true,
                                                    type = ParameterType.Primitive.String,
                                                ),
                                                Parameter.Primitive(
                                                    name = "quantity",
                                                    description = "Item quantity",
                                                    isRequired = true,
                                                    type = ParameterType.Primitive.Integer,
                                                ),
                                            ),
                                    ),
                            ),
                        ),
                )

            SerializerToParameter.convert<ItemList>() shouldBe expected
        }
    }
})
