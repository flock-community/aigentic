package community.flock.aigentic.code.generation.ksp.processor.util

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import community.flock.aigentic.code.generation.ksp.processor.TypeMapper

/**
 * Utility functions for type checking in the KSP processor.
 */
object TypeUtils {
    /**
     * Checks if the given type name represents a List or Array type.
     *
     * @param typeName The qualified name of the type
     * @return true if the type is a List or Array, false otherwise
     */
    fun isListOrArrayType(typeName: String): Boolean =
        typeName.startsWith("kotlin.collections.") &&
            (typeName.contains("List") || typeName.contains("Array"))

    /**
     * Checks if the given declaration is a data class.
     *
     * @param declaration The declaration to check
     * @return true if the declaration is a data class, false otherwise
     */
    fun isDataClass(declaration: KSDeclaration): Boolean = declaration is KSClassDeclaration && declaration.modifiers.contains(Modifier.DATA)

    /**
     * Checks if the given declaration is an enum class.
     *
     * @param declaration The declaration to check
     * @return true if the declaration is an enum class, false otherwise
     */
    fun isEnumClass(declaration: KSDeclaration): Boolean = declaration is KSClassDeclaration && declaration.classKind == ClassKind.ENUM_CLASS

    /**
     * Checks if the given type is a primitive type supported by the TypeMapper.
     *
     * @param typeName The qualified name of the type
     * @return true if the type is a supported primitive type, false otherwise
     */
    fun isPrimitiveType(typeName: String): Boolean = TypeMapper.mapKotlinTypeToPrimitiveType(typeName) != null

    /**
     * Determines the element type of a collection type.
     *
     * @param type The collection type
     * @return The element type, or null if it cannot be determined
     */
    fun getElementType(type: KSType): KSType? = if (type.arguments.isNotEmpty()) type.arguments[0].type?.resolve() else null
}
