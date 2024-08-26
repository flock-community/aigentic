package community.flock.aigentic.platform

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Run
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
    override suspend fun sendRun(
        run: Run,
        agent: Agent,
    ): RunSentResult = platformClient.sendRun(run, agent)

    companion object {
        fun defaultAigenticPlatformClient(
            authentication: Authentication.BasicAuth,
            apiUrl: PlatformApiUrl,
        ): AigenticPlatformClient {
            return AigenticPlatformClient(authentication, apiUrl)
        }
    }
}
