package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.Aigentic
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.getParameter
import community.flock.aigentic.generated.parameter.PersonParameter
import community.flock.aigentic.generated.parameter.UserParameter
import community.flock.aigentic.generated.parameter.initialize
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ObjectParameterTest : DescribeSpec({

    beforeTest {
        Aigentic.initialize()
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

    fun getObjectParameter(
        objectParam: Parameter.Complex.Object,
        name: String,
    ): Parameter.Complex.Object {
        val param = objectParam.parameters.find { it.name == name }
        param shouldNotBe null
        param.shouldBeInstanceOf<Parameter.Complex.Object>()
        return param
    }

    describe("Object Parameter Tests") {

        describe("Description Tests") {
            it("should have correct description for user id parameter") {
                val userParam = UserParameter.parameter
                val idParam = getPrimitiveParameter(userParam, "id")
                idParam.description shouldBe "The id of the user. Must be unique."
            }

            it("should have correct description for user name parameter") {
                val userParam = UserParameter.parameter
                val userNameParam = getPrimitiveParameter(userParam, "userName")
                userNameParam.description shouldBe "The username for login purposes."
            }

            it("should have correct description for person id parameter") {
                val userParam = UserParameter.parameter
                val personParam = getObjectParameter(userParam, "person")
                val personIdParam = getPrimitiveParameter(personParam, "id")
                personIdParam.description shouldBe "Unique identifier for the person"
            }

            it("should have correct description for person name parameter") {
                val userParam = UserParameter.parameter
                val personParam = getObjectParameter(userParam, "person")
                val personNameParam = getPrimitiveParameter(personParam, "name")
                personNameParam.description shouldBe "Full name of the person"
            }

            it("should have correct description for person age parameter") {
                val userParam = UserParameter.parameter
                val personParam = getObjectParameter(userParam, "person")
                val personAgeParam = getPrimitiveParameter(personParam, "age")
                personAgeParam.description shouldBe "Age of the person in years"
            }
        }

        describe("Basic Parameter Tests") {
            it("should have correct name for user parameter") {
                val userParam = UserParameter.parameter
                userParam.name shouldBe "user"
            }

            it("should have correct name for person parameter") {
                val personParam = PersonParameter.parameter
                personParam.name shouldBe "person"
            }

            it("should be accessible from registry for user parameter") {
                val userFromRegistry = getParameter<User>()
                userFromRegistry shouldNotBe null
            }

            it("should be accessible from registry for person parameter") {
                val personFromRegistry = getParameter<Person>()
                personFromRegistry shouldNotBe null
            }
        }

        describe("Nested Object Tests") {
            it("should contain person parameter in user") {
                val userParam = UserParameter.parameter
                val personParam = userParam.parameters.find { it.name == "person" }
                personParam shouldNotBe null
                personParam.shouldBeInstanceOf<Parameter.Complex.Object>()
            }

            it("should have correct number of properties for person") {
                val userParam = UserParameter.parameter
                val personParam = getObjectParameter(userParam, "person")
                personParam.parameters.size shouldBe 3
            }

            it("should have person id as required") {
                val userParam = UserParameter.parameter
                val personParam = getObjectParameter(userParam, "person")
                val idParam = getPrimitiveParameter(personParam, "id")
                idParam.isRequired shouldBe true
            }

            it("should have person name as not required") {
                val userParam = UserParameter.parameter
                val personParam = getObjectParameter(userParam, "person")
                val nameParam = getPrimitiveParameter(personParam, "name")
                nameParam.isRequired shouldBe false
            }

            it("should have person age as required") {
                val userParam = UserParameter.parameter
                val personParam = getObjectParameter(userParam, "person")
                val ageParam = getPrimitiveParameter(personParam, "age")
                ageParam.isRequired shouldBe true
            }
        }

        describe("Deep Nesting Tests") {
            it("should contain level2 parameter in level1") {
                val level1Param = getParameter<Level1>() as Parameter.Complex.Object
                val level2Param = level1Param.parameters.find { it.name == "level2" }
                level2Param shouldNotBe null
                level2Param.shouldBeInstanceOf<Parameter.Complex.Object>()
            }

            it("should contain level3 parameter in level2") {
                val level1Param = getParameter<Level1>() as Parameter.Complex.Object
                val level2Param = getObjectParameter(level1Param, "level2")
                val level3Param = level2Param.parameters.find { it.name == "level3" }
                level3Param shouldNotBe null
                level3Param.shouldBeInstanceOf<Parameter.Complex.Object>()
            }

            it("should have string type for level3 name parameter") {
                val level1Param = getParameter<Level1>() as Parameter.Complex.Object
                val level2Param = getObjectParameter(level1Param, "level2")
                val level3Param = getObjectParameter(level2Param, "level3")
                val nameParam = getPrimitiveParameter(level3Param, "name")
                nameParam.type shouldBe ParameterType.Primitive.String
            }

            it("should have integer type for level3 value parameter") {
                val level1Param = getParameter<Level1>() as Parameter.Complex.Object
                val level2Param = getObjectParameter(level1Param, "level2")
                val level3Param = getObjectParameter(level2Param, "level3")
                val valueParam = getPrimitiveParameter(level3Param, "value")
                valueParam.type shouldBe ParameterType.Primitive.Integer
            }
        }
    }
})
