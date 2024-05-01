package community.flock.aigentic.core.dsl

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class AgentDSL

interface Config<T> {
    fun build(): T
}
