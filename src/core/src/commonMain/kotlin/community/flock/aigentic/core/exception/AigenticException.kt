package community.flock.aigentic.core.exception

internal data class AigenticException(override val message: String, val throwable: Throwable? = null) : Exception(message, throwable)

fun aigenticException(
    message: String,
    throwable: Throwable? = null,
): Nothing = throw AigenticException(message, throwable)
