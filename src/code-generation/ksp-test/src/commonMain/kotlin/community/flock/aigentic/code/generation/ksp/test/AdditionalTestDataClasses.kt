package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.annotations.AigenticParameter

// Deep nesting test classes
@AigenticParameter
data class Level1(val name: String, val level2: Level2)

data class Level2(val name: String, val level3: Level3)

data class Level3(val name: String, val value: Int)

// Edge cases
@AigenticParameter
data class EmptyLike(val dummy: String = "")

@AigenticParameter
data class AllNullable(val name: String?, val age: Int?)
