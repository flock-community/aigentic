package community.flock.aigentic.example.koog

import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.koog.fetchExampleRunPrompt
import community.flock.aigentic.platform.AigenticPlatform
import community.flock.aigentic.platform.client.defaultPlatformApiUrl
import kotlin.system.exitProcess
import ai.koog.prompt.message.Message as KoogMessage

private val liveCheckAigenticPlatformSecret by lazy {
    System.getenv("AIGENTIC_PLATFORM_SECRET").also {
        if (it.isNullOrEmpty()) error("Set 'AIGENTIC_PLATFORM_SECRET' environment variable!")
    }
}

private val liveCheckAigenticPlatformName by lazy { System.getenv("AIGENTIC_PLATFORM_NAME") ?: "koog-example" }

private val liveCheckAigenticPlatformUrl by lazy { System.getenv("AIGENTIC_PLATFORM_URL") ?: defaultPlatformApiUrl }

suspend fun main() {
    val platform =
        AigenticPlatform(
            authentication = Authentication.BasicAuth(username = liveCheckAigenticPlatformName, password = liveCheckAigenticPlatformSecret),
            apiUrl = PlatformApiUrl(liveCheckAigenticPlatformUrl),
        )

    val tags = listOf(RunTag("koog-example"))
    val prompt =
        fetchExampleRunPrompt<WeatherAnswer>(platform, tags, "You are a weather assistant. Use the weather tool to answer questions about the weather.")

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
