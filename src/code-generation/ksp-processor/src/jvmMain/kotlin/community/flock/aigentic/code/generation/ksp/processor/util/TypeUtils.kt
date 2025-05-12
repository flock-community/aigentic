package community.flock.aigentic.code.generation.ksp.processor.util

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import community.flock.aigentic.code.generation.ksp.processor.TypeMapper

object TypeUtils {
    fun isListOrArrayType(typeName: String): Boolean =
        typeName.startsWith("kotlin.collections.") &&
            (typeName.contains("List") || typeName.contains("Array"))

    fun isDataClass(declaration: KSDeclaration): Boolean = declaration is KSClassDeclaration && declaration.modifiers.contains(Modifier.DATA)

    fun isEnumClass(declaration: KSDeclaration): Boolean = declaration is KSClassDeclaration && declaration.classKind == ClassKind.ENUM_CLASS

    fun isPrimitiveType(typeName: String): Boolean = TypeMapper.mapKotlinTypeToPrimitiveType(typeName) != null

    fun getElementType(type: KSType): KSType? = if (type.arguments.isNotEmpty()) type.arguments[0].type?.resolve() else null
}
