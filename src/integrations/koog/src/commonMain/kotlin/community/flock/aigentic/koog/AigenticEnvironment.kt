package community.flock.aigentic.koog

import ai.koog.agents.core.system.getEnvironmentVariableOrNull
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.platform.AigenticPlatform
import community.flock.aigentic.platform.client.defaultPlatformApiUrl

/**
 * Builds an [AigenticPlatform] client from environment variables, the same way Koog's own
 * `addLangfuseExporter` defaults from `LANGFUSE_PUBLIC_KEY`/`LANGFUSE_SECRET_KEY`. Any parameter
 * passed explicitly overrides the corresponding environment variable.
 *
 * @param agentName agent name / basic auth username. Defaults to `AIGENTIC_PLATFORM_NAME`.
 * @param secret basic auth password. Defaults to `AIGENTIC_PLATFORM_SECRET`.
 * @param apiUrl platform API URL. Defaults to `AIGENTIC_PLATFORM_URL`, falling back to [defaultPlatformApiUrl].
 * @param lookupEnv environment variable lookup, injectable for testing.
 */
fun defaultAigenticPlatform(
    agentName: String? = null,
    secret: String? = null,
    apiUrl: String? = null,
    lookupEnv: (String) -> String? = ::getEnvironmentVariableOrNull,
): AigenticPlatform {
    val name =
        agentName ?: lookupEnv("AIGENTIC_PLATFORM_NAME")
            ?: error("Set 'AIGENTIC_PLATFORM_NAME' or pass agentName explicitly")
    val resolvedSecret =
        secret ?: lookupEnv("AIGENTIC_PLATFORM_SECRET")
            ?: error("Set 'AIGENTIC_PLATFORM_SECRET' or pass secret explicitly")
    val url = apiUrl ?: lookupEnv("AIGENTIC_PLATFORM_URL") ?: defaultPlatformApiUrl

    return AigenticPlatform(
        authentication = Authentication.BasicAuth(username = name, password = resolvedSecret),
        apiUrl = PlatformApiUrl(url),
    )
}
