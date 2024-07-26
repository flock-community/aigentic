package community.flock.aigentic.platform

import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.core.platform.RunSentResult
import community.flock.aigentic.gateway.wirespec.GatewayClientErrorDto
import community.flock.aigentic.gateway.wirespec.GatewayEndpoint
import community.flock.aigentic.gateway.wirespec.ServerErrorDto
import community.flock.aigentic.platform.util.createAgent
import community.flock.aigentic.platform.util.createRun
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class AigenticPlatformTest : DescribeSpec({

    withData(
        nameFn = { "Should map ${it.wirespecResponse} to ${it.runSentResult}" },
        TestCase(GatewayEndpoint.Response201Unit(), RunSentResult.Success),
        TestCase(GatewayEndpoint.Response401Unit(), RunSentResult.Unauthorized),
        TestCase(
            GatewayEndpoint.Response400ApplicationJson(GatewayClientErrorDto("invalid request")),
            RunSentResult.Error("invalid request"),
        ),
        TestCase(
            GatewayEndpoint.Response500ApplicationJson(ServerErrorDto("error", "something went wrong")),
            RunSentResult.Error("error - something went wrong"),
        ),
    ) {

        val agent = createAgent()
        val run = createRun()

        val platformClient =
            mockk<GatewayEndpoint>().apply {
                coEvery { gateway(any()) } returns it.wirespecResponse
            }

        val aigenticPlatform =
            AigenticPlatform(
                authentication = Authentication.BasicAuth("username", "password"),
                apiUrl = PlatformApiUrl("http://localhost:8080"),
                platformClient = platformClient,
            )

        val result = aigenticPlatform.sendRun(run, agent)

        result shouldBe it.runSentResult
        coVerify(exactly = 1) { platformClient.gateway(any()) }
    }
})

private data class TestCase<T>(val wirespecResponse: GatewayEndpoint.Response<T>, val runSentResult: RunSentResult)
