package community.flock.aigentic.core.dsl

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class AgentDSL

interface Config<T> {
    fun build(): T
}

inline fun <reified T> Config<T>.builderPropertyMissingErrorMessage(
    fieldName: String,
    setterName: String,
): () -> String {
    return { "Cannot build ${T::class.simpleName}, property '$fieldName' is missing, please use '$setterName' to provide it" }
}
