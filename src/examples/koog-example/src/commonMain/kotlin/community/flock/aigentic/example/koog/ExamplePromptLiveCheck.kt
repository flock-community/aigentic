package community.flock.aigentic.example.koog

import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.koog.defaultAigenticPlatform
import community.flock.aigentic.koog.fetchExampleRunPrompt
import kotlin.system.exitProcess
import ai.koog.prompt.message.Message as KoogMessage

suspend fun main() {
    val tags = listOf(RunTag("koog-example"))
    val (prompt, exampleRunIds) =
        fetchExampleRunPrompt<WeatherAnswer>(
            tags = tags,
            systemPrompt = "You are a weather assistant. Use the weather tool to answer questions about the weather.",
        )

    println("LIVE_CHECK_EXAMPLE_RUN_IDS=${exampleRunIds.map { it.value }}")
    println("LIVE_CHECK_MESSAGE_COUNT=${prompt.messages.size}")
    prompt.messages.forEachIndexed { index, message ->
        val role =
            when (message) {
                is KoogMessage.System -> "SYSTEM"
                is KoogMessage.User -> "USER"
                is KoogMessage.Assistant -> "ASSISTANT"
                else -> message::class.simpleName ?: "UNKNOWN"
            }
        println("LIVE_CHECK_MSG[$index][$role]=${message.textContent()}")
    }

    exitProcess(0)
}
