package community.flock.aigentic.core.platform

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.agent.decode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.jvm.JvmInline

sealed interface Authentication {
    data class Secret(val secret: String) : Authentication

    data class BasicAuth(val username: String, val password: String) : Authentication
}

@JvmInline
value class PlatformApiUrl(val value: String)

interface PlatformClient {
    suspend fun <I : Any, O : Any> sendRun(
        run: Run<O>,
        agent: Agent<I, O>,
        outputSerializer: KSerializer<O>,
    ): RunSentResult

    suspend fun getRuns(tags: List<RunTag>): List<Pair<RunId, Run<String>>>
}

interface Platform {
    val authentication: Authentication.BasicAuth
    val apiUrl: PlatformApiUrl
    val client: PlatformClient
}

suspend inline fun <reified I : Any, reified O : Any> Platform.sendRun(
    run: Run<O>,
    agent: Agent<I, O>,
): RunSentResult = client.sendRun(run, agent, serializer<O>())

suspend inline fun <reified O : Any> Platform.getRuns(tags: List<RunTag>): List<Pair<RunId, Run<O>>> =
    client.getRuns(tags)
        .map { it.first to it.second.decode() }

sealed interface RunSentResult {
    data object Success : RunSentResult
    data object Unauthorized : RunSentResult

    data class Error(val message: String) : RunSentResult
}
