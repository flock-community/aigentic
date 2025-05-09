package community.flock.aigentic.code.generation.ksp.processor.util

import com.google.devtools.ksp.processing.KSPLogger

class ErrorUtils(private val logger: KSPLogger) {
    fun error(message: String): Nothing {
        logger.error(message)
        throw IllegalStateException(message)
    }
}
