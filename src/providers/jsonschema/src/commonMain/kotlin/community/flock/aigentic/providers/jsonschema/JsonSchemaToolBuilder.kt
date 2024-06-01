package community.flock.aigentic.providers.jsonschema

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

fun JsonObjectBuilder.emitPropertiesAndRequired(parameters: List<Parameter>) {
    putJsonObject("properties") {
        emit(parameters)
    }
    putJsonArray("required") {
        parameters.filter { it.isRequired }.forEach { parameter: Parameter ->
            add(parameter.name)
        }
    }
}

private fun JsonObjectBuilder.emit(definition: Parameter) {
    putJsonObject(definition.name) {
        emitType(definition)
        definition.description?.run {
            put("description", this)
        }
        emitSpecificPropertiesIfNecessary(definition)
    }
}

private fun JsonObjectBuilder.emitType(definition: Parameter) =
    when (definition) {
        is Parameter.Complex.Array -> "array"
        is Parameter.Complex.Object -> "object"
        is Parameter.Complex.Enum -> mapPrimitiveType(definition.valueType)
        is Parameter.Primitive -> mapPrimitiveType(definition.type)
    }.run {
        put("type", this)
    }

private fun mapPrimitiveType(type: ParameterType.Primitive) =
    when (type) {
        ParameterType.Primitive.Boolean -> "boolean"
        ParameterType.Primitive.Integer -> "integer"
        ParameterType.Primitive.Number -> "number"
        ParameterType.Primitive.String -> "string"
    }

private fun JsonObjectBuilder.emitSpecificPropertiesIfNecessary(definition: Parameter) =
    when (definition) {
        is Parameter.Complex -> emitSpecificProperties(definition)
        is Parameter.Primitive -> Unit // Primitive types don't have any other properties
    }

private fun JsonObjectBuilder.emitSpecificProperties(definition: Parameter.Complex): Unit =
    when (definition) {
        is Parameter.Complex.Array -> emitSpecificProperties(definition)
        is Parameter.Complex.Enum -> emitSpecificProperties(definition)
        is Parameter.Complex.Object -> emitSpecificProperties(definition)
    }

private fun JsonObjectBuilder.emitSpecificProperties(definition: Parameter.Complex.Array) {
    putJsonObject("items") {
        emitType(definition.itemDefinition)
        emitSpecificPropertiesIfNecessary(definition.itemDefinition)
    }
}

private fun JsonObjectBuilder.emitSpecificProperties(definition: Parameter.Complex.Enum) {
    putJsonArray("enum") {
        definition.values.forEach {
            when (it) {
                is PrimitiveValue.Boolean -> add(it.value)
                is PrimitiveValue.Integer -> add(it.value)
                is PrimitiveValue.Number -> add(it.value)
                is PrimitiveValue.String -> add(it.value)
            }
        }
    }
}

private fun JsonObjectBuilder.emitSpecificProperties(definition: Parameter.Complex.Object) {
    emitPropertiesAndRequired(definition.parameters)
}

private fun JsonObjectBuilder.emit(definitions: List<Parameter>): Unit =
    definitions.forEach { parameter ->
        emit(parameter)
    }
