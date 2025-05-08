package community.flock.aigentic.code.generation.ksp.processor.visitor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import community.flock.aigentic.code.generation.ksp.processor.TypeMapper
import community.flock.aigentic.code.generation.ksp.processor.builder.ParameterBuilder
import community.flock.aigentic.code.generation.ksp.processor.qualifiedName
import community.flock.aigentic.code.generation.ksp.processor.util.ErrorUtils
import community.flock.aigentic.code.generation.ksp.processor.util.StringUtils
import community.flock.aigentic.code.generation.ksp.processor.util.TypeUtils
import community.flock.aigentic.core.tool.ParameterType

class PropertyVisitor(
    private val resolver: Resolver,
    private val errorUtils: ErrorUtils,
) {
    fun visitProperty(
        property: KSPropertyDeclaration,
        indentLevel: Int = 0,
    ): String {
        val name = property.simpleName.asString()
        val type = property.type.resolve()
        val typeName = type.declaration.qualifiedName?.asString() ?: "Unknown"
        val isNullable = type.nullability == Nullability.NULLABLE
        val description = property.getAnnotationValue("Description", "value")

        return when {
            TypeUtils.isPrimitiveType(typeName) ->
                visitPrimitiveProperty(name, typeName, isNullable, indentLevel, description)
            TypeUtils.isListOrArrayType(typeName) && type.arguments.isNotEmpty() ->
                visitArrayProperty(name, type, isNullable, indentLevel, description)
            TypeUtils.isDataClass(type.declaration) ->
                visitObjectProperty(name, type.declaration as KSClassDeclaration, isNullable, indentLevel, description)
            TypeUtils.isEnumClass(type.declaration) ->
                visitEnumProperty(name, type.declaration as KSClassDeclaration, isNullable, indentLevel, description)
            else ->
                errorUtils.error("Unsupported property type '${type.declaration.qualifiedName?.asString()}' for property: $name")
        }
    }

    private fun visitPrimitiveProperty(
        name: String,
        typeName: String,
        isNullable: Boolean,
        indentLevel: Int = 0,
        description: String? = null,
    ): String {
        val primitiveType =
            TypeMapper.mapKotlinTypeToPrimitiveType(typeName)
                ?: errorUtils.error("No primitive type mapping found for $typeName")

        val content = ParameterBuilder.buildPrimitiveParameter(name, primitiveType, !isNullable, description)
        return StringUtils.indent(content, indentLevel)
    }

    private fun visitArrayProperty(
        name: String,
        type: KSType,
        isNullable: Boolean,
        indentLevel: Int = 0,
        description: String? = null,
    ): String {
        val elementType =
            TypeUtils.getElementType(type)
                ?: errorUtils.error("Could not determine element type for array property: $name")

        val elementTypeName = elementType.declaration.qualifiedName?.asString() ?: "Unknown"
        val elementTypeDeclaration = elementType.declaration as? KSClassDeclaration

        val itemDefinition =
            when {
                TypeUtils.isPrimitiveType(elementTypeName) ->
                    visitPrimitiveItemDefinition("item", elementTypeName)
                TypeUtils.isListOrArrayType(elementTypeName) ->
                    visitArrayProperty("item", elementType, false, 0)
                elementTypeDeclaration != null && TypeUtils.isEnumClass(elementTypeDeclaration) ->
                    visitEnumProperty("item", elementTypeDeclaration, false, 0)
                elementTypeDeclaration != null && TypeUtils.isDataClass(elementTypeDeclaration) ->
                    visitObjectProperty("item", elementTypeDeclaration, false, 0)
                else ->
                    errorUtils.error("Unsupported element type for array property: $name")
            }

        val indentedItemDefinition = StringUtils.indent(itemDefinition, 2)
        val content = ParameterBuilder.buildComplexArrayParameter(name, !isNullable, indentedItemDefinition, description)

        return StringUtils.indent(content, indentLevel)
    }

    private fun visitPrimitiveItemDefinition(
        name: String,
        elementTypeName: String,
    ): String {
        val primitiveType =
            TypeMapper.mapKotlinTypeToPrimitiveType(elementTypeName)
                ?: errorUtils.error("No primitive type mapping found for $elementTypeName")

        return ParameterBuilder.buildPrimitiveParameter(name, primitiveType, true)
    }

    private fun visitObjectProperty(
        name: String,
        typeDeclaration: KSClassDeclaration,
        isNullable: Boolean,
        indentLevel: Int = 0,
        description: String? = null,
    ): String {
        val nestedParameters = visitNestedParameters(typeDeclaration, 1)
        val content = ParameterBuilder.buildComplexObjectParameter(name, !isNullable, nestedParameters, description)

        return StringUtils.indent(content, indentLevel)
    }

    private fun visitNestedParameters(
        classDeclaration: KSClassDeclaration,
        indentLevel: Int = 0,
    ): String {
        return classDeclaration.getAllProperties()
            .map { visitProperty(it, indentLevel) }
            .joinToString(",\n")
    }

    private fun visitEnumProperty(
        name: String,
        enumClassDeclaration: KSClassDeclaration,
        isNullable: Boolean,
        indentLevel: Int = 0,
        description: String? = null,
    ): String {
        val enumEntries =
            enumClassDeclaration.declarations
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == com.google.devtools.ksp.symbol.ClassKind.ENUM_ENTRY }
                .map { it.simpleName.asString() }

        val enumValues =
            enumEntries
                .map { "PrimitiveValue.String(\"$it\")" }
                .joinToString(", ")

        val content =
            ParameterBuilder.buildComplexEnumParameter(
                name = name,
                isRequired = !isNullable,
                values = enumValues,
                valueType = ParameterType.Primitive.String.qualifiedName,
                description = description,
            )

        return StringUtils.indent(content, indentLevel)
    }

    private fun KSPropertyDeclaration.getAnnotationValue(
        annotationName: String,
        argumentName: String,
    ): String? =
        annotations
            .find { it.shortName.asString() == annotationName }
            ?.arguments
            ?.find { it.name?.asString() == argumentName }
            ?.value as? String
}
