package community.flock.aigentic.platform

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.platform.client.PlatformGatewayClient
import community.flock.aigentic.gateway.wirespec.GatewayEndpoint
import community.flock.aigentic.platform.mapper.toDto

data class AigenticPlatform(
    override val authentication: Authentication.BasicAuth,
    override val apiUrl: PlatformApiUrl,
    private val platformClient: PlatformGatewayClient = defaultAigenticPlatformClient(authentication, apiUrl)
) : Platform {

    override suspend fun sendRun(run: Run, agent: Agent) {
        val runDto = run.toDto(agent)
        val request = GatewayEndpoint.RequestApplicationJson(runDto)
        platformClient.gateway(request)
    }

    companion object {

        fun defaultAigenticPlatformClient(
            authentication: Authentication.BasicAuth,
            apiUrl: PlatformApiUrl
        ): PlatformGatewayClient  {
            return PlatformGatewayClient(authentication, apiUrl)
        }
    }
}
