package community.flock.aigentic.platform

import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.gateway.wirespec.GatewayEndpoint
import community.flock.aigentic.platform.util.createAgent
import community.flock.aigentic.platform.util.createRun
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coVerify
import io.mockk.mockk

class AigenticPlatformTest : DescribeSpec({

    describe("AigenticPlatform") {
        it("Should send run to gateway endpoint") {

            val agent = createAgent()
            val run = createRun()

            val platformClient = mockk<GatewayEndpoint>(relaxed = true)

            val aigenticPlatform =
                AigenticPlatform(
                    authentication = Authentication.BasicAuth("username", "password"),
                    apiUrl = PlatformApiUrl("http://localhost:8080"),
                    platformClient = platformClient,
                )

            aigenticPlatform.sendRun(run, agent)

            coVerify { platformClient.gateway(any()) }
        }
    }
})
