package community.flock.aigentic.platform

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.core.platform.RunSentResult
import community.flock.aigentic.platform.client.AigenticPlatformClient

data class AigenticPlatform(
    override val authentication: Authentication.BasicAuth,
    override val apiUrl: PlatformApiUrl,
    private val platformClient: AigenticPlatformClient = defaultAigenticPlatformClient(authentication, apiUrl),
) : Platform {
    override suspend fun <I, O> sendRun(
        run: Run,
        agent: Agent<I, O>,
    ): RunSentResult = platformClient.sendRun(run, agent)

    override suspend fun getRuns(tags: List<RunTag>): List<Pair<RunId, Run>> = platformClient.getRuns(tags)

    companion object {
        fun defaultAigenticPlatformClient(
            authentication: Authentication.BasicAuth,
            apiUrl: PlatformApiUrl,
        ): AigenticPlatformClient {
            return AigenticPlatformClient(authentication, apiUrl)
        }
    }
}
