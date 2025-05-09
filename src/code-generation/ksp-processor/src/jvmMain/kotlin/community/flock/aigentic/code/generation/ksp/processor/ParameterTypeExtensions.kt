package community.flock.aigentic.code.generation.ksp.processor

import community.flock.aigentic.core.tool.ParameterType

val ParameterType.qualifiedName: String
    get() =
        when (this) {
            ParameterType.Primitive.String -> "ParameterType.Primitive.String"
            ParameterType.Primitive.Integer -> "ParameterType.Primitive.Integer"
            ParameterType.Primitive.Number -> "ParameterType.Primitive.Number"
            ParameterType.Primitive.Boolean -> "ParameterType.Primitive.Boolean"
            ParameterType.Complex.Array -> "ParameterType.Complex.Array"
            ParameterType.Complex.Enum -> "ParameterType.Complex.Enum"
            ParameterType.Complex.Object -> "ParameterType.Complex.Object"
        }
