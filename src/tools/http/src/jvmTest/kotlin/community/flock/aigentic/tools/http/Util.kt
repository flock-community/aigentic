package http

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType

fun createPrimitiveParameter(
    name: String, type: ParameterType.Primitive = ParameterType.Primitive.String, isRequired: Boolean = true
) = Parameter.Primitive(
    name = name, description = null, isRequired = isRequired, type = type
)

fun createObjectParameter(
    name: String,
    isRequired: Boolean = true,
    parameters: List<Parameter>
) = Parameter.Complex.Object(
    name = name, description = null, isRequired = isRequired, parameters = parameters
)
