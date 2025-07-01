package community.flock.aigentic.core.tool

import community.flock.aigentic.core.annotations.Description
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.serializer

object SerializerToParameter {

    inline fun <reified T : Any> convert(): Parameter? {
        try {
            val serializer = serializer<T>()
            return Parameter.Complex.Object(
                name = T::class.simpleName ?: error("No name found"),
                description = serializer.descriptor.annotations.getDescription(),
                isRequired = true,
                parameters = serializer.descriptor.parameters()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun SerialDescriptor.parameters(): List<Parameter> {
        val range = 0 ..< elementsCount
        return range.map {
            val descriptor = getElementDescriptor(it)
            when(descriptor.kind) {
                StructureKind.LIST -> parameterComplexArray(it)
                StructureKind.CLASS -> parameterComplexObject(it)
                SerialKind.ENUM -> parameterComplexEnum(it)
                else -> parameterPrimitive(it)
            }
        }
    }

    fun SerialDescriptor.parameterPrimitive(idx:Int): Parameter.Primitive {
        val annotations = getElementAnnotations(idx)
        val descriptor = getElementDescriptor(idx)
        val name = getElementName(idx)
        return Parameter.Primitive(
            name = name,
            description = annotations.getDescription(),
            isRequired = !descriptor.isNullable,
            type = descriptor.kind.conver()
        )

    }

    fun SerialDescriptor.parameterComplexEnum(idx:Int): Parameter.Complex.Enum {
        val annotations = getElementAnnotations(idx)
        val descriptor = getElementDescriptor(idx)
        val name = getElementName(idx)
        val range = 0 ..< descriptor.elementsCount
        val values = range.map {descriptor.getElementName(it) }.map { PrimitiveValue.String(it) }
        return Parameter.Complex.Enum(
            name = name,
            description = annotations.getDescription(),
            isRequired = !descriptor.isNullable,
            default = values.firstOrNull(),
            values = values,
            valueType = ParameterType.Primitive.String
        )

    }
    fun SerialDescriptor.parameterComplexArray(idx:Int): Parameter.Complex.Array {
        val annotations = getElementAnnotations(idx)
        val descriptor = getElementDescriptor(idx)
        val name = getElementName(idx)
        return Parameter.Complex.Array(
            name = name,
            description = annotations.getDescription(),
            isRequired = !descriptor.isNullable,
            itemDefinition = Parameter.Primitive(
                name = name,
                description = null,
                isRequired = false,
                type = ParameterType.Primitive.String,
            )
        )

    }

    fun SerialDescriptor.parameterComplexObject(idx:Int): Parameter.Complex.Object {
        val annotations = getElementAnnotations(idx)
        val descriptor = getElementDescriptor(idx)
        val name = getElementName(idx)
        return Parameter.Complex.Object(
            name = name,
            description = annotations.getDescription(),
            isRequired = !descriptor.isNullable,
            parameters = descriptor.parameters()
        )

    }

    fun SerialKind.conver(): ParameterType.Primitive = when(this) {
        PrimitiveKind.DOUBLE -> ParameterType.Primitive.Number
        PrimitiveKind.FLOAT -> ParameterType.Primitive.Number
        PrimitiveKind.BOOLEAN -> ParameterType.Primitive.Boolean
        PrimitiveKind.INT -> ParameterType.Primitive.Integer
        PrimitiveKind.LONG -> ParameterType.Primitive.Integer
        PrimitiveKind.SHORT -> ParameterType.Primitive.String
        PrimitiveKind.STRING -> ParameterType.Primitive.String
        PrimitiveKind.CHAR -> ParameterType.Primitive.String

        is SerialKind.ENUM -> TODO()
        is SerialKind.CONTEXTUAL -> TODO()
        PolymorphicKind.OPEN -> TODO()
        PolymorphicKind.SEALED -> TODO()
        PrimitiveKind.BYTE -> TODO()
        StructureKind.CLASS -> TODO()
        StructureKind.LIST -> TODO()
        StructureKind.MAP -> TODO()
        StructureKind.OBJECT -> TODO()
    }

    fun List<Annotation>.getDescription(): String? =
        filterIsInstance<Description>().map { it.value }.firstOrNull()

}
