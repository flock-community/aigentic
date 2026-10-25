package community.flock.aigentic.providers.jsonschema

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

enum class SchemaDialect(
    internal val nullType: Boolean,
    internal val requireEveryProperty: Boolean,
) {
    OPENAPI_SCHEMA(nullType = false, requireEveryProperty = false),
    JSON_SCHEMA(nullType = true, requireEveryProperty = false),
    OPENAI_JSON_SCHEMA(nullType = true, requireEveryProperty = true),
}

fun Parameter.toResponseSchema(dialect: SchemaDialect): JsonObject =
    when (this) {
        is Parameter.Complex.Object -> parameters
        else -> listOf(this)
    }.toObjectSchema(dialect)

fun List<Parameter>.toObjectSchema(dialect: SchemaDialect): JsonObject =
    buildJsonObject {
        put("type", "object")
        emitPropertiesAndRequired(this@toObjectSchema, dialect)
    }

fun JsonObjectBuilder.emitPropertiesAndRequired(
    parameters: List<Parameter>,
    dialect: SchemaDialect,
) {
    putJsonObject("properties") {
        emit(parameters, dialect)
    }
    putJsonArray("required") {
        parameters.filter { it.isRequired || dialect.requireEveryProperty }.forEach { parameter: Parameter ->
            add(parameter.name)
        }
    }
}

private fun JsonObjectBuilder.emit(
    definition: Parameter,
    dialect: SchemaDialect,
) {
    putJsonObject(definition.name) {
        emitType(definition, dialect)
        definition.description?.run {
            put("description", this)
        }
        emitSpecificPropertiesIfNecessary(definition, dialect)
    }
}

private fun JsonObjectBuilder.emitType(
    definition: Parameter,
    dialect: SchemaDialect,
) {
    val type =
        when (definition) {
            is Parameter.Complex.Array -> "array"
            is Parameter.Complex.Object -> "object"
            is Parameter.Complex.Enum -> mapPrimitiveType(definition.valueType)
            is Parameter.Primitive -> mapPrimitiveType(definition.type)
        }
    when {
        definition.isRequired -> {
            put("type", type)
        }

        dialect.nullType -> {
            putJsonArray("type") {
                add(type)
                add("null")
            }
        }

        else -> {
            put("type", type)
            put("nullable", true)
        }
    }
}

private fun mapPrimitiveType(type: ParameterType.Primitive) =
    when (type) {
        ParameterType.Primitive.Boolean -> "boolean"
        ParameterType.Primitive.Integer -> "integer"
        ParameterType.Primitive.Number -> "number"
        ParameterType.Primitive.String -> "string"
    }

private fun JsonObjectBuilder.emitSpecificPropertiesIfNecessary(
    definition: Parameter,
    dialect: SchemaDialect,
) = when (definition) {
    is Parameter.Complex -> emitSpecificProperties(definition, dialect)
    is Parameter.Primitive -> Unit // Primitive types don't have any other properties
}

private fun JsonObjectBuilder.emitSpecificProperties(
    definition: Parameter.Complex,
    dialect: SchemaDialect,
): Unit =
    when (definition) {
        is Parameter.Complex.Array -> emitSpecificProperties(definition, dialect)
        is Parameter.Complex.Enum -> emitSpecificProperties(definition, dialect)
        is Parameter.Complex.Object -> emitSpecificProperties(definition, dialect)
    }

private fun JsonObjectBuilder.emitSpecificProperties(
    definition: Parameter.Complex.Array,
    dialect: SchemaDialect,
) {
    putJsonObject("items") {
        emitType(definition.itemDefinition, dialect)
        emitSpecificPropertiesIfNecessary(definition.itemDefinition, dialect)
    }
}

private fun JsonObjectBuilder.emitSpecificProperties(
    definition: Parameter.Complex.Enum,
    dialect: SchemaDialect,
) {
    putJsonArray("enum") {
        definition.values.forEach {
            when (it) {
                is PrimitiveValue.Boolean -> add(it.value)
                is PrimitiveValue.Integer -> add(it.value)
                is PrimitiveValue.Number -> add(it.value)
                is PrimitiveValue.String -> add(it.value)
            }
        }
        if (!definition.isRequired && dialect.nullType) {
            add(JsonNull)
        }
    }
}

private fun JsonObjectBuilder.emitSpecificProperties(
    definition: Parameter.Complex.Object,
    dialect: SchemaDialect,
) {
    emitPropertiesAndRequired(definition.parameters, dialect)
}

private fun JsonObjectBuilder.emit(
    definitions: List<Parameter>,
    dialect: SchemaDialect,
): Unit =
    definitions.forEach { parameter ->
        emit(parameter, dialect)
    }
