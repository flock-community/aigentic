package community.flock.aigentic.code.generation.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class Description(
    val value: String,
)
