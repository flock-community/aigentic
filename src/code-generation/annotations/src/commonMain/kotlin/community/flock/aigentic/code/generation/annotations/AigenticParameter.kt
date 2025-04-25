package community.flock.aigentic.code.generation.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class AigenticParameter(
    val description: String = "",
)
