package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.tool.ParameterRegistry
import community.flock.aigentic.core.tool.getParameter
import community.flock.aigentic.generated.parameter.initialize
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.generated.parameter.PersonParameter
import community.flock.aigentic.generated.parameter.UserParameter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ObjectParameterTest {

    @BeforeTest
    fun setup() {
        ParameterRegistry.initialize()
    }

    // Helper methods
    private fun getObjectParameter(objectParam: Parameter.Complex.Object, name: String): Parameter.Complex.Object {
        val param = objectParam.parameters.find { it.name == name }
        assertNotNull(param, "$name parameter should exist")
        assertTrue(param is Parameter.Complex.Object, "$name parameter should be a Complex.Object")
        return param
    }

    private fun getPrimitiveParameter(objectParam: Parameter.Complex.Object, name: String): Parameter.Primitive {
        val param = objectParam.parameters.find { it.name == name }
        assertNotNull(param, "$name parameter should exist")
        assertTrue(param is Parameter.Primitive, "$name parameter should be a Primitive")
        return param
    }

    // Description Tests
    @Test
    fun testUserIdParameterShouldHaveCorrectDescription() {
        val userParam = UserParameter.parameter
        val idParam = getPrimitiveParameter(userParam, "id")
        assertEquals("The id of the user. Must be unique.", idParam.description, "Id parameter should have correct description")
    }

    @Test
    fun testUserNameParameterShouldHaveCorrectDescription() {
        val userParam = UserParameter.parameter
        val userNameParam = getPrimitiveParameter(userParam, "userName")
        assertEquals("The username for login purposes.", userNameParam.description, "UserName parameter should have correct description")
    }

    @Test
    fun testPersonIdParameterShouldHaveCorrectDescription() {
        val userParam = UserParameter.parameter
        val personParam = getObjectParameter(userParam, "person")
        val personIdParam = getPrimitiveParameter(personParam, "id")
        assertEquals("Unique identifier for the person", personIdParam.description, "Id parameter in Person should have correct description")
    }

    @Test
    fun testPersonNameParameterShouldHaveCorrectDescription() {
        val userParam = UserParameter.parameter
        val personParam = getObjectParameter(userParam, "person")
        val personNameParam = getPrimitiveParameter(personParam, "name")
        assertEquals("Full name of the person", personNameParam.description, "Name parameter in Person should have correct description")
    }

    @Test
    fun testPersonAgeParameterShouldHaveCorrectDescription() {
        val userParam = UserParameter.parameter
        val personParam = getObjectParameter(userParam, "person")
        val personAgeParam = getPrimitiveParameter(personParam, "age")
        assertEquals("Age of the person in years", personAgeParam.description, "Age parameter in Person should have correct description")
    }

    // Basic Parameter Tests
    @Test
    fun testUserParameterShouldHaveCorrectName() {
        val userParam = UserParameter.parameter
        assertEquals("user", userParam.name, "User parameter should have name 'user'")
    }

    @Test
    fun testPersonParameterShouldHaveCorrectName() {
        val personParam = PersonParameter.parameter
        assertEquals("person", personParam.name, "Person parameter should have name 'person'")
    }

    @Test
    fun testUserParameterShouldBeAccessibleFromRegistry() {
        val userFromRegistry = getParameter<User>()
        assertNotNull(userFromRegistry, "User parameter should be accessible from registry")
    }

    @Test
    fun testPersonParameterShouldBeAccessibleFromRegistry() {
        val personFromRegistry = getParameter<Person>()
        assertNotNull(personFromRegistry, "Person parameter should be accessible from registry")
    }

    // Nested Object Tests
    @Test
    fun testUserShouldContainPersonParameter() {
        val userParam = UserParameter.parameter as Parameter.Complex.Object
        val personParam = userParam.parameters.find { it.name == "person" }
        assertNotNull(personParam, "Person parameter should exist")
        assertTrue(personParam is Parameter.Complex.Object, "Person parameter should be a Complex.Object")
    }

    @Test
    fun testPersonShouldHaveCorrectNumberOfProperties() {
        val userParam = UserParameter.parameter as Parameter.Complex.Object
        val personParam = getObjectParameter(userParam, "person")
        assertEquals(3, personParam.parameters.size, "Person should have 3 properties")
    }

    @Test
    fun testPersonIdShouldBeRequired() {
        val userParam = UserParameter.parameter as Parameter.Complex.Object
        val personParam = getObjectParameter(userParam, "person")
        val idParam = getPrimitiveParameter(personParam, "id")
        assertTrue(idParam.isRequired, "Id should be required")
    }

    @Test
    fun testPersonNameShouldNotBeRequired() {
        val userParam = UserParameter.parameter as Parameter.Complex.Object
        val personParam = getObjectParameter(userParam, "person")
        val nameParam = getPrimitiveParameter(personParam, "name")
        assertFalse(nameParam.isRequired, "Name should not be required (nullable)")
    }

    @Test
    fun testPersonAgeShouldBeRequired() {
        val userParam = UserParameter.parameter as Parameter.Complex.Object
        val personParam = getObjectParameter(userParam, "person")
        val ageParam = getPrimitiveParameter(personParam, "age")
        assertTrue(ageParam.isRequired, "Age should be required")
    }

    // Deep Nesting Tests
    @Test
    fun testLevel1ShouldContainLevel2Parameter() {
        val level1Param = getParameter<Level1>() as Parameter.Complex.Object
        val level2Param = level1Param.parameters.find { it.name == "level2" }
        assertNotNull(level2Param, "Level2 parameter should exist")
        assertTrue(level2Param is Parameter.Complex.Object, "Level2 parameter should be a Complex.Object")
    }

    @Test
    fun testLevel2ShouldContainLevel3Parameter() {
        val level1Param = getParameter<Level1>() as Parameter.Complex.Object
        val level2Param = getObjectParameter(level1Param, "level2")
        val level3Param = level2Param.parameters.find { it.name == "level3" }
        assertNotNull(level3Param, "Level3 parameter should exist")
        assertTrue(level3Param is Parameter.Complex.Object, "Level3 parameter should be a Complex.Object")
    }

    @Test
    fun testLevel3NameParameterShouldHaveStringType() {
        val level1Param = getParameter<Level1>() as Parameter.Complex.Object
        val level2Param = getObjectParameter(level1Param, "level2")
        val level3Param = getObjectParameter(level2Param, "level3")
        val nameParam = getPrimitiveParameter(level3Param, "name")
        assertEquals(ParameterType.Primitive.String, nameParam.type, "Name should be mapped to String type")
    }

    @Test
    fun testLevel3ValueParameterShouldHaveIntegerType() {
        val level1Param = getParameter<Level1>() as Parameter.Complex.Object
        val level2Param = getObjectParameter(level1Param, "level2")
        val level3Param = getObjectParameter(level2Param, "level3")
        val valueParam = getPrimitiveParameter(level3Param, "value")
        assertEquals(ParameterType.Primitive.Integer, valueParam.type, "Value should be mapped to Integer type")
    }
}
