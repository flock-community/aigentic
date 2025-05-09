package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.annotations.AigenticParameter

// Test class for nullable enum property
@AigenticParameter
data class NullableEnumProperty(
    val id: String,
    val status: TodoStatus?,
)

// Test class for enum property with default value
@AigenticParameter
data class DefaultEnumProperty(
    val id: String,
    val status: TodoStatus = TodoStatus.COMPLETED,
)

// Test class with multiple enum properties
@AigenticParameter
data class MultipleEnumProperties(
    val id: String,
    val primaryStatus: TodoStatus,
    val secondaryStatus: TodoStatus,
)

// Test class with nested object containing enum property
@AigenticParameter
data class NestedEnumProperty(
    val id: String,
    val task: TaskWithEnum,
)

data class TaskWithEnum(
    val name: String,
    val status: TodoStatus,
)
