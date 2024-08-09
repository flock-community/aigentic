package community.flock.aigentic.example

import java.util.Base64

object FileReader {
    fun readFileBase64(path: String): String {
        val inputStream =
            this::class.java.getResource(path)?.openStream()
                ?: throw IllegalArgumentException("Resource not found: $path")
        val bytes = inputStream.use { it.readBytes() }
        return Base64.getEncoder().encodeToString(bytes)
    }
}
