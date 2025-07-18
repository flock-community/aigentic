package community.flock.aigentic.core.tool

import community.flock.aigentic.core.exception.aigenticException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmInline

@PublishedApi
internal inline fun <reified T : Any> getParameter(): Parameter =
    when (val param = SerializerToParameter.convert<T>()) {
        is Parameter.Complex.Object, is Parameter.Complex.Enum -> param
        is Parameter.Complex.Array -> {
            if (param.itemDefinition.type is ParameterType.Primitive) {
                simpleTypeNotSupportedException("Collection<param.itemDefinition.type.toString()>")
            } else {
                param
            }
        }
        is Parameter.Primitive -> simpleTypeNotSupportedException(param.type.toString())
    }

@PublishedApi
internal fun simpleTypeNotSupportedException(type: String): Nothing =
    aigenticException(
        """
    Parameter of type $type is not supported. Please use a @AigenticParameter annotated class instead.
""",
    )

@PublishedApi
internal fun Parameter.getStringValue(arguments: JsonObject): String = arguments.getValue(name).jsonPrimitive.content

sealed class Parameter(
    open val name: String,
    open val description: String?,
    open val isRequired: Boolean,
    open val type: ParameterType,
) {
    fun copy(
        name: String,
        description: String? = null,
    ) = when (this) {
        is Primitive -> Primitive(name, description, isRequired, type)
        is Complex.Enum -> Complex.Enum(name, description, isRequired, default, values, valueType)
        is Complex.Object -> Complex.Object(name, description, isRequired, parameters)
        is Complex.Array -> Complex.Array(name, description, isRequired, itemDefinition)
    }

    data class Primitive(
        override val name: String,
        override val description: String?,
        override val isRequired: Boolean,
        override val type: ParameterType.Primitive,
    ) : Parameter(name, description, isRequired, type)

    sealed class Complex(name: String, description: String?, isRequired: Boolean, type: ParameterType) :
        Parameter(name, description, isRequired, type) {
            data class Enum(
                override val name: String,
                override val description: String?,
                override val isRequired: Boolean,
                val default: PrimitiveValue<*>?,
                val values: List<PrimitiveValue<*>>,
                val valueType: ParameterType.Primitive,
            ) : Complex(name, description, isRequired, ParameterType.Complex.Enum)

            data class Object(
                override val name: String,
                override val description: String?,
                override val isRequired: Boolean,
                val parameters: List<Parameter>,
            ) : Complex(name, description, isRequired, ParameterType.Complex.Object)

            data class Array(
                override val name: String,
                override val description: String?,
                override val isRequired: Boolean,
                val itemDefinition: Parameter,
            ) : Complex(name, description, isRequired, ParameterType.Complex.Array)
        }
}

sealed interface ParameterType {
    sealed interface Primitive : ParameterType {
        data object String : Primitive
        data object Number : Primitive
        data object Integer : Primitive
        data object Boolean : Primitive
    }

    sealed interface Complex : ParameterType {
        data object Array : ParameterType
        data object Enum : ParameterType
        data object Object : ParameterType
    }
}

sealed interface PrimitiveValue<Type> {
    val value: Type

    @JvmInline
    value class String(override val value: kotlin.String) : PrimitiveValue<kotlin.String> {
        companion object {
            fun fromString(value: kotlin.String) = String(value)
        }
    }

    @JvmInline
    value class Number(override val value: kotlin.Number) : PrimitiveValue<kotlin.Number> {
        companion object {
            fun fromString(value: kotlin.String) = Number(value.toInt())
        }
    }

    @JvmInline
    value class Integer(override val value: kotlin.Number) : PrimitiveValue<kotlin.Number> {
        companion object {
            fun fromString(value: kotlin.String) = Integer(value.toInt())
        }
    }

    @JvmInline
    value class Boolean(override val value: kotlin.Boolean) : PrimitiveValue<kotlin.Boolean> {
        companion object {
            fun fromString(value: kotlin.String) = Boolean(value.toBoolean())
        }
    }
}
