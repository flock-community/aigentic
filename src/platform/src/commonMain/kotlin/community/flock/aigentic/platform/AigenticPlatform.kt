package community.flock.aigentic.platform

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.core.platform.RunSentResult
import community.flock.aigentic.gateway.wirespec.GatewayEndpoint
import community.flock.aigentic.platform.client.PlatformGatewayClient
import community.flock.aigentic.platform.mapper.toDto

data class AigenticPlatform(
    override val authentication: Authentication.BasicAuth,
    override val apiUrl: PlatformApiUrl,
    private val platformClient: GatewayEndpoint = defaultAigenticPlatformClient(authentication, apiUrl),
) : Platform {
    override suspend fun sendRun(
        run: Run,
        agent: Agent,
    ): RunSentResult {
        val runDto = run.toDto(agent)
        val request = GatewayEndpoint.RequestApplicationJson(runDto)
        return when (val response = platformClient.gateway(request)) {
            is GatewayEndpoint.Response201Unit -> RunSentResult.Success
            is GatewayEndpoint.Response401Unit -> RunSentResult.Unauthorized
            is GatewayEndpoint.Response400ApplicationJson -> RunSentResult.Error(response.content.body.message)
            is GatewayEndpoint.Response500ApplicationJson ->
                RunSentResult.Error(
                    "${response.content.body.name} - ${response.content.body.description}",
                )
        }
    }

    companion object {
        fun defaultAigenticPlatformClient(
            authentication: Authentication.BasicAuth,
            apiUrl: PlatformApiUrl,
        ): PlatformGatewayClient {
            return PlatformGatewayClient(authentication, apiUrl)
        }
    }
}
