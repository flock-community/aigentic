package community.flock.aigentic.koog.mapper

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import kotlinx.serialization.json.JsonObject

fun ToolDescriptor.toAigenticTool(): Tool =
    object : Tool {
        override val name: ToolName = ToolName(this@toAigenticTool.name)
        override val description: String = this@toAigenticTool.description
        override val parameters: List<Parameter> =
            requiredParameters.map { it.toAigenticParameter(isRequired = true) } +
                optionalParameters.map { it.toAigenticParameter(isRequired = false) }
        override val handler: suspend (toolArguments: JsonObject) -> String =
            { error("Tool descriptions imported from Koog carry no handler") }
    }

fun ToolParameterDescriptor.toAigenticParameter(isRequired: Boolean): Parameter =
    type.toAigenticParameter(name = name, description = description, isRequired = isRequired)

private fun ToolParameterType.toAigenticParameter(
    name: String,
    description: String?,
    isRequired: Boolean,
): Parameter =
    when (this) {
        ToolParameterType.String -> {
            Parameter.Primitive(name, description, isRequired, ParameterType.Primitive.String)
        }

        ToolParameterType.Integer -> {
            Parameter.Primitive(name, description, isRequired, ParameterType.Primitive.Integer)
        }

        ToolParameterType.Float -> {
            Parameter.Primitive(name, description, isRequired, ParameterType.Primitive.Number)
        }

        ToolParameterType.Boolean -> {
            Parameter.Primitive(name, description, isRequired, ParameterType.Primitive.Boolean)
        }

        is ToolParameterType.Enum -> {
            Parameter.Complex.Enum(
                name = name,
                description = description,
                isRequired = isRequired,
                default = null,
                values = entries.map { PrimitiveValue.String(it) },
                valueType = ParameterType.Primitive.String,
            )
        }

        is ToolParameterType.List -> {
            Parameter.Complex.Array(
                name = name,
                description = description,
                isRequired = isRequired,
                itemDefinition = itemsType.toAigenticParameter(name = "item", description = null, isRequired = true),
            )
        }

        is ToolParameterType.Object -> {
            Parameter.Complex.Object(
                name = name,
                description = description,
                isRequired = isRequired,
                parameters = properties.map { it.toAigenticParameter(isRequired = it.name in requiredProperties) },
            )
        }

        // Koog uses AnyOf to represent nullability and Null as a standalone marker type; Aigentic has no
        // equivalent, so these fall back to a plain string parameter rather than dropping the field.
        ToolParameterType.Null, is ToolParameterType.AnyOf -> {
            Parameter.Primitive(name, description, isRequired, ParameterType.Primitive.String)
        }
    }
