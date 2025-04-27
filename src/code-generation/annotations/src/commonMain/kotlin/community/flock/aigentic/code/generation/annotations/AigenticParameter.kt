package community.flock.aigentic.code.generation.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MetaSerializable

@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MetaSerializable
annotation class AigenticParameter(
    val description: String = "",
)
