package community.flock.aigentic.platform

import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.core.platform.PlatformClient
import community.flock.aigentic.platform.client.AigenticPlatformClient

data class AigenticPlatform(
    override val authentication: Authentication.BasicAuth,
    override val apiUrl: PlatformApiUrl,
    override val client: PlatformClient = defaultAigenticPlatformClient(authentication, apiUrl),
) : Platform {
    companion object {
        fun defaultAigenticPlatformClient(
            authentication: Authentication.BasicAuth,
            apiUrl: PlatformApiUrl,
        ): AigenticPlatformClient {
            return AigenticPlatformClient(authentication, apiUrl)
        }
    }
}
