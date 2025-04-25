package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.code.generation.annotations.AigenticParameter
import community.flock.aigentic.code.generation.annotations.Description

@AigenticParameter(description = "A test class")
data class Todo(
    @Description("Current status of the todo item")
    val status: TodoStatus,
)

enum class TodoStatus {
    COMPLETED,
    IN_PROGRESS,
}
