package community.flock.aigentic.core.tool

import community.flock.aigentic.core.annotations.Description
import kotlinx.coroutines.NonCancellable.children
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.serializer

@OptIn(ExperimentalSerializationApi::class)
object SerializerToParameter {
    data class Element(
        val name: String,
        val descriptor: SerialDescriptor,
        val annotations: List<Annotation>,
    )

    inline fun <reified T : Any> convert(): Parameter {
        val serializer = serializer<T>()
        val name = T::class.simpleName ?: error("No name found")
        return serializer.descriptor.element(name).toParameter()
    }

    fun Element.toParameter() =
        when (descriptor.kind) {
            StructureKind.LIST -> toArray()
            StructureKind.CLASS -> toObject()
            StructureKind.OBJECT -> toObject()
            PolymorphicKind.SEALED -> toObject()
            SerialKind.ENUM -> toEnum()
            StructureKind.MAP -> toMap()
            PrimitiveKind.DOUBLE -> toPrimitive(ParameterType.Primitive.Number)
            PrimitiveKind.FLOAT -> toPrimitive(ParameterType.Primitive.Number)
            PrimitiveKind.BOOLEAN -> toPrimitive(ParameterType.Primitive.Boolean)
            PrimitiveKind.INT -> toPrimitive(ParameterType.Primitive.Integer)
            PrimitiveKind.LONG -> toPrimitive(ParameterType.Primitive.Integer)
            PrimitiveKind.SHORT -> toPrimitive(ParameterType.Primitive.String)
            PrimitiveKind.STRING -> toPrimitive(ParameterType.Primitive.String)
            PrimitiveKind.CHAR -> toPrimitive(ParameterType.Primitive.String)
            PolymorphicKind.OPEN -> TODO()
            PrimitiveKind.BYTE -> TODO()
            SerialKind.CONTEXTUAL -> TODO()
        }

    fun SerialDescriptor.parameters(): List<Parameter> {
        return range()
            .map { children(it) }
            .map { it.toParameter() }
    }

    fun SerialDescriptor.element(name: String) =
        Element(
            name = name,
            descriptor = this,
            annotations = annotations,
        )

    fun SerialDescriptor.children(idx: Int) =
        Element(
            name = getElementName(idx),
            descriptor = getElementDescriptor(idx),
            annotations = getElementAnnotations(idx),
        )

    fun SerialDescriptor.range() = 0..<elementsCount

    fun Element.toPrimitive(type: ParameterType.Primitive) =
        Parameter.Primitive(
            name = name,
            description = annotations.getDescription(),
            isRequired = !descriptor.isNullable,
            type = type,
        )

    fun Element.toEnum(): Parameter.Complex.Enum {
        val values =
            descriptor
                .range()
                .map { descriptor.getElementName(it) }
                .map { PrimitiveValue.String(it) }
        return Parameter.Complex.Enum(
            name = name,
            description = annotations.getDescription(),
            isRequired = !descriptor.isNullable,
            default = values.firstOrNull(),
            values = values,
            valueType = ParameterType.Primitive.String,
        )
    }

    fun Element.toArray(): Parameter.Complex.Array {
        return Parameter.Complex.Array(
            name = name,
            description = annotations.getDescription(),
            isRequired = !descriptor.isNullable,
            itemDefinition =
                descriptor.parameters()
                    .firstOrNull()
                    ?.copy("Item")
                    ?: error("No item definition found"),
        )
    }

    fun Element.toObject() =
        Parameter.Complex.Object(
            name = name,
            description = annotations.getDescription(),
            isRequired = !descriptor.isNullable,
            parameters = descriptor.parameters(),
        )

    fun Element.toMap(): Parameter.Complex.Object {
        return Parameter.Complex.Object(
            name = name,
            description = annotations.getDescription(),
            isRequired = !descriptor.isNullable,
            parameters = descriptor.parameters(),
        )
    }

    fun List<Annotation>.getDescription(): String? = filterIsInstance<Description>().map { it.value }.firstOrNull()
}
