package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.code.generation.annotations.AigenticParameter

/**
 * Test data classes for primitive types.
 */

@AigenticParameter
data class NumberTypes(
    val floatValue: Float,
    val doubleValue: Double,
    val nullableFloat: Float?
)

@AigenticParameter
data class BooleanTypes(
    val booleanValue: Boolean,
    val nullableBoolean: Boolean?
)

@AigenticParameter
data class AllPrimitiveTypes(
    val stringValue: String,
    val intValue: Int,
    val longValue: Long,
    val floatValue: Float,
    val doubleValue: Double,
    val booleanValue: Boolean
)
