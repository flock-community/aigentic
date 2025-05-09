package community.flock.aigentic.example

import java.util.Base64

val openAIAPIKey: String by lazy {
    System.getenv("OPENAI_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'OPENAI_KEY' environment variable!")
    }
}

val geminiKey: String by lazy {
    System.getenv("GEMINI_API_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'GEMINI_API_KEY' environment variable!")
    }
}

object FileReader {
    fun readFileBase64(path: String): String {
        val inputStream =
            this::class.java.getResource(path)?.openStream()
                ?: throw IllegalArgumentException("Resource not found: $path")
        val bytes = inputStream.use { it.readBytes() }
        return Base64.getEncoder().encodeToString(bytes)
    }
}
