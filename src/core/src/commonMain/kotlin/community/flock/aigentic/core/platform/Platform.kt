package community.flock.aigentic.core.platform

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.RunTag
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

    suspend fun <I : Any, O : Any> sendRun(
        run: Run<O>,
        agent: Agent<I, O>,
    ): RunSentResult

    suspend fun <O : Any> getRuns(tags: List<RunTag>): List<Pair<RunId, Run<O>>>
}

sealed interface RunSentResult {
    data object Success : RunSentResult
    data object Unauthorized : RunSentResult

    data class Error(val message: String) : RunSentResult
}
