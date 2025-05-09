package community.flock.aigentic.core.tool

import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

inline fun <reified T : Any> getParameter(): Parameter.Complex.Object? {
    return ParameterRegistry.getParameter(T::class)
}

object ParameterRegistry {
    private val parameterRegistry = mutableMapOf<ParameterIdentifier, Parameter.Complex.Object>()

    init {
        register(
            "kotlin",
            "Unit",
            Parameter.Complex.Object(
                name = "unit",
                description = null,
                isRequired = true,
                parameters = emptyList(),
            ),
        )
    }

    fun register(
        packageName: String,
        simpleName: String,
        parameter: Parameter.Complex.Object,
    ) {
        parameterRegistry[ParameterIdentifier(packageName, simpleName)] = parameter
    }

    fun <T : Any> getParameter(clazz: KClass<T>): Parameter.Complex.Object? =
        parameterRegistry.entries.firstOrNull {
            it.key.simpleName ==
                clazz.simpleName
        }?.value
}

@JvmInline
value class ParameterIdentifier(private val qualifiedName: String) {
    constructor(packageName: String, simpleName: String) : this("$packageName.$simpleName")

    val packageName: String get() = qualifiedName.substringBeforeLast(".")
    val simpleName: String get() = qualifiedName.substringAfterLast(".")
}
