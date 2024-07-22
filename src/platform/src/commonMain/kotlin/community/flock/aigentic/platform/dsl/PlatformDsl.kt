package community.flock.aigentic.platform.dsl

import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.AgentDSL
import community.flock.aigentic.core.dsl.Config
import community.flock.aigentic.core.dsl.builderPropertyMissingErrorMessage
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.platform.AigenticPlatform

fun AgentConfig.platform(platformConfig: PlatformConfig.() -> Unit) =
    PlatformConfig().apply(platformConfig).build().also {
        this.platform(it)
    }

@AgentDSL
class PlatformConfig() : Config<Platform> {

    private var name: String? = null
    private var secret: String? = null
    private var apiUrl: String? = null

    fun PlatformConfig.name(name: String) {
        this.name = name
    }

    fun PlatformConfig.secret(secret: String) {
        this.secret = secret
    }

    fun PlatformConfig.apiUrl(apiUrl: String) {
        this.apiUrl = apiUrl
    }

    override fun build(): Platform =
        AigenticPlatform(
            authentication = Authentication.BasicAuth(
                username = checkNotNull(
                    name,
                    builderPropertyMissingErrorMessage("name", "platform { name() }")
                ),
                password = checkNotNull(
                    secret,
                    builderPropertyMissingErrorMessage("secret", "platform { secret() }")
                )
            ),
            apiUrl = PlatformApiUrl(checkNotNull(apiUrl, builderPropertyMissingErrorMessage("apiUrl", "platform { apiUrl() }"))),
        )
}
