package community.flock.aigentic.core.exception

data class AigenticException(val msg: String, val throwable: Throwable? = null) : Exception(msg, throwable)

fun aigenticException(
    message: String,
    throwable: Throwable? = null,
): Nothing = throw AigenticException(message, throwable)
