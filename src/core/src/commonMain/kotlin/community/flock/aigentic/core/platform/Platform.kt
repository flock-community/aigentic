package community.flock.aigentic.core.platform

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.AgentRun
import community.flock.aigentic.core.agent.Expected
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.agent.decode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.jvm.JvmInline

sealed interface Authentication {
    data class Secret(
        val secret: String,
    ) : Authentication

    data class BasicAuth(
        val username: String,
        val password: String,
    ) : Authentication
}

@JvmInline
value class PlatformApiUrl(
    val value: String,
)

interface PlatformClient {
    suspend fun <I : Any, O : Any> sendRun(
        run: AgentRun<O>,
        agent: Agent<I, O>,
        outputSerializer: KSerializer<O>,
        expected: Expected<O>?,
    ): RunSentResult

    suspend fun <O : Any> addToEvaluationSet(
        runId: RunId,
        evaluationSet: String,
        expected: O,
        outputSerializer: KSerializer<O>,
    ): EvaluationSubmitResult

    suspend fun getRuns(tags: List<RunTag>): List<Pair<RunId, AgentRun<String>>>
}

interface Platform {
    val authentication: Authentication.BasicAuth
    val apiUrl: PlatformApiUrl
    val client: PlatformClient
}

suspend inline fun <reified I : Any, reified O : Any> Platform.sendRun(
    run: AgentRun<O>,
    agent: Agent<I, O>,
    expected: Expected<O>? = null,
): RunSentResult = client.sendRun(run, agent, serializer<O>(), expected)

suspend inline fun <reified O : Any> Platform.addToEvaluationSet(
    runId: RunId,
    evaluationSet: String,
    expected: O,
): EvaluationSubmitResult = client.addToEvaluationSet(runId, evaluationSet, expected, serializer<O>())

suspend inline fun <reified O : Any> Platform.getRuns(tags: List<RunTag>): List<Pair<RunId, AgentRun<O>>> =
    client
        .getRuns(tags)
        .map { (runId: RunId, agentRunString: AgentRun<String>) ->
            runId to agentRunString.decode()
        }

sealed interface RunSentResult {
    data class Success(
        val runId: RunId?,
    ) : RunSentResult

    data object Unauthorized : RunSentResult

    data class Error(
        val message: String,
    ) : RunSentResult
}

sealed interface EvaluationSubmitResult {
    data object Success : EvaluationSubmitResult

    data object Unauthorized : EvaluationSubmitResult

    data object NotFound : EvaluationSubmitResult

    data class Error(
        val message: String,
    ) : EvaluationSubmitResult
}
