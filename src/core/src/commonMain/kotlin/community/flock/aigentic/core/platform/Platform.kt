package community.flock.aigentic.core.platform

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Run
import kotlin.jvm.JvmInline

sealed interface Authentication {
    data class Secret(val secret: String) : Authentication

    data class BasicAuth(val username: String, val password: String) : Authentication
}

@JvmInline
value class PlatformApiUrl(val value: String)

interface Platform {
    val authentication: Authentication.BasicAuth
    val apiUrl: PlatformApiUrl

    suspend fun sendRun(
        run: Run,
        agent: Agent,
    ): RunSentResult
}

sealed interface RunSentResult {
    data object Success : RunSentResult
    data object Unauthorized : RunSentResult

    data class Error(val message: String) : RunSentResult
}
