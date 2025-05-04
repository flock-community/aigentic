package community.flock.aigentic.code.generation.ksp.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import community.flock.aigentic.code.generation.ksp.processor.builder.ParameterBuilder
import community.flock.aigentic.code.generation.ksp.processor.util.ErrorUtils
import community.flock.aigentic.code.generation.ksp.processor.visitor.PropertyVisitor

class AigenticParameterProcessor(
    private val codeGenerator: CodeGenerator,
    logger: KSPLogger,
) : SymbolProcessor {
    private val generatedPackageName = "community.flock.aigentic.generated.parameter"
    private val generatedMetadataClasses = mutableListOf<Pair<String, String>>()
    private val errorUtils = ErrorUtils(logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("community.flock.aigentic.core.annotations.AigenticParameter")
        val propertyVisitor = PropertyVisitor(resolver, errorUtils)

        symbols.filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() && it.isDataClass() }
            .forEach { classDeclaration ->
                generateParameterClass(classDeclaration, propertyVisitor)
            }

        return emptyList()
    }

    override fun finish() {
        generateParameterRegistry()
    }

    private fun KSClassDeclaration.isDataClass(): Boolean = modifiers.contains(Modifier.DATA)

    private fun KSAnnotated.getAnnotationValue(
        annotationName: String,
        argumentName: String,
    ): String? =
        annotations
            .find { it.shortName.asString() == annotationName }
            ?.arguments
            ?.find { it.name?.asString() == argumentName }
            ?.value as? String

    private fun generateParameterClass(
        classDeclaration: KSClassDeclaration,
        propertyVisitor: PropertyVisitor,
    ) {
        val className = classDeclaration.simpleName.asString()
        val parameterClassName = "${className}Parameter"

        generatedMetadataClasses.add(generatedPackageName to parameterClassName)

        val description = classDeclaration.getAnnotationValue("AigenticParameter", "description") ?: ""

        val properties =
            classDeclaration.getAllProperties()
                .map { propertyVisitor.visitProperty(it, 2) }
                .joinToString(",\n")

        val fileContent =
            ParameterBuilder.buildParameterClass(
                parameterClassName = parameterClassName,
                className = className,
                properties = properties,
                generatedPackageName = generatedPackageName,
                description = description,
            )

        writeToFile(parameterClassName, fileContent, classDeclaration.containingFile!!)
    }

    private fun writeToFile(
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
