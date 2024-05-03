package community.flock.aigentic.example

import community.flock.aigentic.example.HackerNewsSpec.spec
import kotlinx.coroutines.runBlocking

private val openAIAPIKey = System.getenv("OPENAI_KEY").also {
    if (it.isNullOrEmpty()) error("Set 'OPENAI_KEY' environment variable!")
}

object HackerNewsSpec {
    val spec = this::class.java.getResource("/hackernews.json")!!.readText(Charsets.UTF_8)
}

fun main(): Unit = runBlocking {
    runOpenAPIAgent(openAIAPIKey, spec)
}
