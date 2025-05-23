package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.annotations.AigenticParameter

// Test class for nullable array items
@AigenticParameter
data class NullableItemList(
    val id: String,
    val items: List<String?>,
)

// Test class for nullable array
@AigenticParameter
data class NullableArrayList(
    val id: String,
    val items: List<String>?,
)

// Test class for nested arrays
@AigenticParameter
data class NestedArrayList(
    val id: String,
    val items: List<List<String>>,
)

// Test class for enum arrays
@AigenticParameter
data class EnumArrayList(
    val id: String,
    val statuses: List<TodoStatus>,
)
