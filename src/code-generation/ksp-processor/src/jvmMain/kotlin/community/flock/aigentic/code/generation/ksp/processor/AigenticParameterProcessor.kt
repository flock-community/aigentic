package community.flock.aigentic.code.generation.ksp.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.validate
import community.flock.aigentic.core.tool.ParameterType

class AigenticParameterProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    private val generatedPackageName = "community.flock.aigentic.generated.parameter"

    private val generatedMetadataClasses = mutableListOf<Pair<String, String>>()

    private fun indentString(
        str: String,
        indentLevel: Int,
    ): String =
        str.lines().joinToString("\n") { line ->
            if (line.isBlank()) line else " ".repeat(4 * indentLevel) + line
        }

    private fun stringOrNull(value: String?): String = if (value.isNullOrEmpty()) "null" else "\"$value\""

    private fun createPrimitiveParameterTemplate(
        name: String,
        typeName: String,
        isRequired: Boolean,
        description: String? = null,
    ): String {
        val parameterType = TypeMapper.mapKotlinTypeToPrimitiveType(typeName) ?: ParameterType.Primitive.String

        return """
        |Parameter.Primitive(
        |    name = "$name",
        |    description = ${stringOrNull(description)},
        |    isRequired = $isRequired,
        |    type = ${parameterType.qualifiedName}
        |)
            """.trimMargin()
    }

    private fun KSAnnotated.getAnnotationValue(
        annotationName: String,
        argumentName: String,
    ): String? =
        annotations
            .find { it.shortName.asString() == annotationName }
            ?.arguments
            ?.find { it.name?.asString() == argumentName }
            ?.value as? String

    private fun createComplexObjectParameterTemplate(
        name: String,
        isRequired: Boolean,
        parameters: String,
        description: String? = null,
    ): String =
        """
        |Parameter.Complex.Object(
        |    name = "$name",
        |    description = ${stringOrNull(description)},
        |    isRequired = $isRequired,
        |    parameters = listOf(
        |$parameters
        |    )
        |)
        """.trimMargin()

    private fun createComplexArrayParameterTemplate(
        name: String,
        isRequired: Boolean,
        itemDefinition: String,
        description: String? = null,
    ): String =
        """
        |Parameter.Complex.Array(
        |    name = "$name",
        |    description = ${stringOrNull(description)},
        |    isRequired = $isRequired,
        |    itemDefinition =
        |$itemDefinition
        |)
        """.trimMargin()

    private fun createComplexEnumParameterTemplate(
        name: String,
        isRequired: Boolean,
        values: String,
        valueType: String,
        description: String? = null,
    ): String =
        """
        |Parameter.Complex.Enum(
        |    name = "$name",
        |    description = ${stringOrNull(description)},
        |    isRequired = $isRequired,
        |    default = null,
        |    values = listOf($values),
        |    valueType = $valueType,
        |)
        """.trimMargin()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("community.flock.aigentic.core.annotations.AigenticParameter")

        symbols.filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() && it.isDataClass() }
            .forEach { classDeclaration ->
                generateParameterClass(classDeclaration, resolver)
            }

        return emptyList()
    }

    override fun finish() {
        generateParameterRegistry()
    }

    private fun KSClassDeclaration.isDataClass(): Boolean = modifiers.contains(Modifier.DATA)

    private fun generateParameterClass(
        classDeclaration: KSClassDeclaration,
        resolver: Resolver,
    ) {
        val className = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()
        val parameterClassName = "${className}Parameter"

        generatedMetadataClasses.add(generatedPackageName to parameterClassName)

        val description = classDeclaration.getAnnotationValue("AigenticParameter", "description") ?: ""

        val properties =
            classDeclaration.getAllProperties()
                .map { createParameterDefinitionForProperty(it, resolver, 2) }
                .joinToString(",\n")

        val fileContent = buildParameterClassContent(packageName, parameterClassName, className, properties, description)
        writeToFile(packageName, parameterClassName, fileContent, classDeclaration.containingFile!!)
    }

    private fun buildParameterClassContent(
        packageName: String,
        parameterClassName: String,
        className: String,
        properties: String,
        description: String = "",
    ): String {
        val parameterName = className.replaceFirstChar { it.lowercase() }
        val parameterObject = createComplexObjectParameterTemplate(parameterName, true, properties, description)

        val indentedParameterObject =
            parameterObject.lines().mapIndexed { index, line ->
                if (index == 0) line else "    $line"
            }.joinToString("\n")

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

    private fun writeToFile(
        packageName: String,
        fileName: String,
        content: String,
        sourceFile: KSFile,
    ) {
        val dependencies = Dependencies(true, sourceFile)
        val outputFile =
            codeGenerator.createNewFile(
                dependencies = dependencies,
                packageName = generatedPackageName,
                fileName = fileName,
                extensionName = "kt",
            )
        outputFile.write(content.toByteArray())
    }

    private fun createParameterDefinitionForProperty(
        property: KSPropertyDeclaration,
        resolver: Resolver,
        indentLevel: Int = 0,
    ): String {
        val name = property.simpleName.asString()
        val type = property.type.resolve()
        val typeName = type.declaration.qualifiedName?.asString() ?: "Unknown"
        val isNullable = type.nullability == Nullability.NULLABLE

        val description = property.getAnnotationValue("Description", "value")

        val primitiveType = TypeMapper.mapKotlinTypeToPrimitiveType(typeName)

        return when {
            primitiveType != null ->
                generatePrimitiveParameter(name, typeName, isNullable, indentLevel, description)
            isListOrArrayType(typeName) && type.arguments.isNotEmpty() ->
                generateArrayParameter(name, type, isNullable, resolver, indentLevel, description)
            isDataClass(type.declaration) ->
                generateObjectParameter(name, type.declaration as KSClassDeclaration, isNullable, resolver, indentLevel, description)
            isEnumClass(type.declaration) ->
                generateEnumParameter(name, type.declaration as KSClassDeclaration, isNullable, indentLevel, description)
            else ->
                throw UnsupportedOperationException("Unsupported property type '${type.declaration.qualifiedName?.asString()}' for property: $name")
        }
    }

    private fun isListOrArrayType(typeName: String): Boolean =
        typeName.startsWith("kotlin.collections.") &&
            (typeName.contains("List") || typeName.contains("Array"))

    private fun isDataClass(declaration: KSDeclaration): Boolean = declaration is KSClassDeclaration && declaration.modifiers.contains(Modifier.DATA)

    private fun isEnumClass(declaration: KSDeclaration): Boolean = declaration is KSClassDeclaration && declaration.classKind == ClassKind.ENUM_CLASS

    private fun generatePrimitiveParameter(
        name: String,
        typeName: String,
        isNullable: Boolean,
        indentLevel: Int = 0,
        description: String? = null,
    ): String {
        val content = createPrimitiveParameterTemplate(name, typeName, !isNullable, description)
        return indentString(content, indentLevel)
    }

    private fun generateArrayParameter(
        name: String,
        type: KSType,
        isNullable: Boolean,
        resolver: Resolver,
        indentLevel: Int = 0,
        description: String? = null,
    ): String {
        val elementType = type.arguments[0].type?.resolve()
        val elementTypeName = elementType?.declaration?.qualifiedName?.asString() ?: "Unknown"
        val elementTypeDeclaration = elementType?.declaration as? KSClassDeclaration

        val itemDefinition =
            when {
                TypeMapper.mapKotlinTypeToPrimitiveType(elementTypeName) != null ->
                    generatePrimitiveItemDefinition(name, elementTypeName, 0)
                isListOrArrayType(elementTypeName) && elementType != null -> {
                    generateArrayParameter(
                        name = "item",
                        type = elementType,
                        isNullable = false,
                        resolver = resolver,
                        indentLevel = 0,
                    )
                }
                elementTypeDeclaration != null && elementTypeDeclaration.classKind == ClassKind.ENUM_CLASS -> {
                    generateEnumParameter(
                        name = "item",
                        enumClassDeclaration = elementTypeDeclaration,
                        isNullable = false,
                        indentLevel = 0,
                    )
                }
                elementTypeDeclaration != null && elementTypeDeclaration.modifiers.contains(Modifier.DATA) -> {
                    val nestedParams =
                        elementTypeDeclaration.getAllProperties()
                            .map { createParameterDefinitionForProperty(it, resolver, 0) }
                            .joinToString(",\n")

                    val indentedParams = indentString(nestedParams, 2)

                    createComplexObjectParameterTemplate(name, true, indentedParams)
                }
                else ->
                    error("Unsupported element type for array property: $name")
            }

        val indentedItemDefinition = indentString(itemDefinition, 2)

        val content = createComplexArrayParameterTemplate(name, !isNullable, indentedItemDefinition, description)

        return indentString(content, indentLevel)
    }

    private fun generatePrimitiveItemDefinition(
        name: String,
        elementTypeName: String,
        indentLevel: Int = 0,
    ): String {
        val content = createPrimitiveParameterTemplate(name, elementTypeName, true)
        return indentString(content, indentLevel)
    }

    private fun generateObjectParameter(
        name: String,
        typeDeclaration: KSClassDeclaration,
        isNullable: Boolean,
        resolver: Resolver,
        indentLevel: Int = 0,
        description: String? = null,
    ): String {
        val nestedParameters = generateNestedParameters(typeDeclaration, resolver, 1)

        val content = createComplexObjectParameterTemplate(name, !isNullable, nestedParameters, description)

        return indentString(content, indentLevel)
    }

    private fun generateNestedParameters(
        classDeclaration: KSClassDeclaration,
        resolver: Resolver,
        indentLevel: Int = 0,
    ): String {
        val params =
            classDeclaration.getAllProperties()
                .map { createParameterDefinitionForProperty(it, resolver, indentLevel) }
                .joinToString(",\n")

        return params
    }

    private fun generateEnumParameter(
        name: String,
        enumClassDeclaration: KSClassDeclaration,
        isNullable: Boolean,
        indentLevel: Int = 0,
        description: String? = null,
    ): String {
        val enumEntries =
            enumClassDeclaration.declarations
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ENUM_ENTRY }
                .map { it.simpleName.asString() }

        val enumValues =
            enumEntries
                .map { "PrimitiveValue.String(\"$it\")" }
                .joinToString(", ")

        val content =
            createComplexEnumParameterTemplate(
                name = name,
                isRequired = !isNullable,
                values = enumValues,
                valueType = ParameterType.Primitive.String.qualifiedName,
                description = description,
            )

        return indentString(content, indentLevel)
    }

    private fun generateParameterRegistry() {
        if (generatedMetadataClasses.isEmpty()) return

        val content = buildRegistryInitializerContent()
        val outputFile =
            codeGenerator.createNewFile(
                dependencies = Dependencies(false),
                packageName = generatedPackageName,
                fileName = "AigenticInitializer",
                extensionName = "kt",
            )
        outputFile.write(content.toByteArray())
    }

    private fun buildRegistryInitializerContent(): String =
        """
        |package $generatedPackageName
        |
        |import community.flock.aigentic.core.Aigentic
        |import community.flock.aigentic.core.tool.ParameterRegistry
        |
        |fun ParameterRegistry.initialize() {
        |    ${generatedMetadataClasses.joinToString("\n    ") { (_, className) -> "$generatedPackageName.$className" }}
        |}
        |
        |fun Aigentic.initialize() {
        |    ParameterRegistry.initialize()
        |}
        """.trimMargin()
}
