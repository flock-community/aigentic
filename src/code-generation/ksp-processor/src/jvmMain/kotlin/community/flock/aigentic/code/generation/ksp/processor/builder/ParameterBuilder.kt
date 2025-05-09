package community.flock.aigentic.code.generation.ksp.processor.builder

import community.flock.aigentic.code.generation.ksp.processor.qualifiedName
import community.flock.aigentic.code.generation.ksp.processor.util.StringUtils
import community.flock.aigentic.core.tool.ParameterType

object ParameterBuilder {
    fun buildPrimitiveParameter(
        name: String,
        type: ParameterType.Primitive,
        isRequired: Boolean,
        description: String? = null,
    ): String =
        """
        |Parameter.Primitive(
        |    name = "$name",
        |    description = ${StringUtils.stringOrNull(description)},
        |    isRequired = $isRequired,
        |    type = ${type.qualifiedName}
        |)
        """.trimMargin()

    fun buildComplexObjectParameter(
        name: String,
        isRequired: Boolean,
        parameters: String,
        description: String? = null,
    ): String =
        """
        |Parameter.Complex.Object(
        |    name = "$name",
        |    description = ${StringUtils.stringOrNull(description)},
        |    isRequired = $isRequired,
        |    parameters = listOf(
        |$parameters
        |    )
        |)
        """.trimMargin()

    fun buildComplexArrayParameter(
        name: String,
        isRequired: Boolean,
        itemDefinition: String,
        description: String? = null,
    ): String =
        """
        |Parameter.Complex.Array(
        |    name = "$name",
        |    description = ${StringUtils.stringOrNull(description)},
        |    isRequired = $isRequired,
        |    itemDefinition =
        |$itemDefinition
        |)
        """.trimMargin()

    fun buildComplexEnumParameter(
        name: String,
        isRequired: Boolean,
        values: String,
        valueType: String,
        description: String? = null,
    ): String =
        """
        |Parameter.Complex.Enum(
        |    name = "$name",
        |    description = ${StringUtils.stringOrNull(description)},
        |    isRequired = $isRequired,
        |    default = null,
        |    values = listOf($values),
        |    valueType = $valueType,
        |)
        """.trimMargin()

    fun buildParameterClass(
        parameterClassName: String,
        className: String,
        properties: String,
        generatedPackageName: String,
        description: String = "",
    ): String {
        val parameterName = className.replaceFirstChar { it.lowercase() }
        val parameterObject = buildComplexObjectParameter(parameterName, true, properties, description)

        val indentedParameterObject = StringUtils.indent(parameterObject, 1)

        return """
            |package $generatedPackageName
            |
            |import community.flock.aigentic.core.tool.Parameter
            |import community.flock.aigentic.core.tool.ParameterType
            |import community.flock.aigentic.core.tool.PrimitiveValue
            |import community.flock.aigentic.core.tool.ParameterRegistry
            |
            |object $parameterClassName {
            |    val parameter = $indentedParameterObject
            |
            |    init {
            |        ParameterRegistry.register("$generatedPackageName", "$className", parameter)
            |    }
            |}
            """.trimMargin()
    }
}
