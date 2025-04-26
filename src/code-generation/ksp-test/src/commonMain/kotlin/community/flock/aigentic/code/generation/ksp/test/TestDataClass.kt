package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.code.generation.annotations.AigenticParameter
import community.flock.aigentic.code.generation.annotations.Description

@AigenticParameter
data class User(
    @Description("The id of the user. Must be unique.")
    val id: String,
    @Description("The username for login purposes.")
    val userName: String,
    val person: Person,
)

@AigenticParameter
data class Person(
    @Description("Unique identifier for the person")
    val id: String,
    @Description("Full name of the person")
    val name: String?,
    @Description("Age of the person in years")
    val age: Int,
)

@AigenticParameter
data class Company(
    val name: String,
    val employees: List<Employee>,
)

data class Employee(
    val name: String,
    val position: String,
)

@AigenticParameter
data class TaggedItem(
    @Description("Unique identifier for the tagged item")
    val id: String,
    @Description("Display name of the item")
    val name: String,
    @Description("List of tags associated with this item")
    val tags: List<String>,
)
