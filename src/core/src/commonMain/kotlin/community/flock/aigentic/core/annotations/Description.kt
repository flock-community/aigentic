package community.flock.aigentic.core.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class Description(
    val value: String,
)
