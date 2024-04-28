package community.flock.aigentic.core.logging

interface Logger {

    fun warning(message: String)
}

data object SimpleLogger : Logger {
    override fun warning(message: String) = println("Warning: $message")
}
