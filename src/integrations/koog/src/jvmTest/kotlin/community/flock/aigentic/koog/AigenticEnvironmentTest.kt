package community.flock.aigentic.koog

import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.platform.client.defaultPlatformApiUrl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class AigenticEnvironmentTest :
    DescribeSpec({

        describe("defaultAigenticPlatform") {

            it("builds a platform from environment variables when nothing is passed explicitly") {
                val env =
                    mapOf(
                        "AIGENTIC_PLATFORM_NAME" to "env-agent",
                        "AIGENTIC_PLATFORM_SECRET" to "env-secret",
                        "AIGENTIC_PLATFORM_URL" to "https://example.test/",
                    )

                val platform = defaultAigenticPlatform(lookupEnv = env::get)

                platform.authentication shouldBe Authentication.BasicAuth(username = "env-agent", password = "env-secret")
                platform.apiUrl shouldBe PlatformApiUrl("https://example.test/")
            }

            it("falls back to defaultPlatformApiUrl when AIGENTIC_PLATFORM_URL is absent") {
                val env =
                    mapOf(
                        "AIGENTIC_PLATFORM_NAME" to "env-agent",
                        "AIGENTIC_PLATFORM_SECRET" to "env-secret",
                    )

                val platform = defaultAigenticPlatform(lookupEnv = env::get)

                platform.apiUrl shouldBe PlatformApiUrl(defaultPlatformApiUrl)
            }

            it("throws a clear error when AIGENTIC_PLATFORM_NAME is missing") {
                val env = mapOf("AIGENTIC_PLATFORM_SECRET" to "env-secret")

                val exception =
                    shouldThrow<IllegalStateException> {
                        defaultAigenticPlatform(lookupEnv = env::get)
                    }

                exception.message shouldBe "Set 'AIGENTIC_PLATFORM_NAME' or pass agentName explicitly"
            }

            it("throws a clear error when AIGENTIC_PLATFORM_SECRET is missing") {
                val env = mapOf("AIGENTIC_PLATFORM_NAME" to "env-agent")

                val exception =
                    shouldThrow<IllegalStateException> {
                        defaultAigenticPlatform(lookupEnv = env::get)
                    }

                exception.message shouldBe "Set 'AIGENTIC_PLATFORM_SECRET' or pass secret explicitly"
            }

            it("prefers explicit parameters over environment variables") {
                val env =
                    mapOf(
                        "AIGENTIC_PLATFORM_NAME" to "env-agent",
                        "AIGENTIC_PLATFORM_SECRET" to "env-secret",
                        "AIGENTIC_PLATFORM_URL" to "https://env.test/",
                    )

                val platform =
                    defaultAigenticPlatform(
                        agentName = "explicit-agent",
                        secret = "explicit-secret",
                        apiUrl = "https://explicit.test/",
                        lookupEnv = env::get,
                    )

                platform.authentication shouldBe Authentication.BasicAuth(username = "explicit-agent", password = "explicit-secret")
                platform.apiUrl shouldBe PlatformApiUrl("https://explicit.test/")
            }
        }
    })
