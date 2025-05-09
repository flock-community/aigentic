package community.flock.aigentic.code.generation.ksp.processor

import community.flock.aigentic.core.tool.ParameterType

object TypeMapper {
    private val typeMap =
        mapOf(
            "kotlin.String" to ParameterType.Primitive.String,
            "kotlin.Int" to ParameterType.Primitive.Integer,
            "kotlin.Long" to ParameterType.Primitive.Integer,
            "kotlin.Short" to ParameterType.Primitive.Integer,
            "kotlin.Byte" to ParameterType.Primitive.Integer,
            "kotlin.Float" to ParameterType.Primitive.Number,
            "kotlin.Double" to ParameterType.Primitive.Number,
            "kotlin.Boolean" to ParameterType.Primitive.Boolean,
        )

    fun mapKotlinTypeToPrimitiveType(typeName: String): ParameterType.Primitive? = typeMap[typeName]
}
