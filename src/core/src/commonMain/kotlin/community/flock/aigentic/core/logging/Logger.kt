package community.flock.aigentic.core.logging

interface Logger {
    fun warning(message: String)

    fun error(message: String) = warning(message)
}

data object SimpleLogger : Logger {
    override fun warning(message: String) = println("Warning: $message")

    override fun error(message: String) = println("Error: $message")
}
