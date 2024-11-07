package community.flock.aigentic.platform.client

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
import io.mockk.mockk

class AigenticPlatformClientTest : DescribeSpec({

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

        val platformEndpoints =
            mockk<PlatformEndpoints>().apply {
                coEvery { gateway(any()) } returns it.wirespecResponse
            }

        val basicAuth = Authentication.BasicAuth("username", "password")
        val apiUrl = PlatformApiUrl("http://localhost:8080")

        val client =
            AigenticPlatformClient(
                basicAuth = basicAuth,
                apiUrl = apiUrl,
                platformEndpoints,
            )

        val result = client.sendRun(run, agent)

        result shouldBe it.runSentResult
    }
})

private data class TestCase<T>(val wirespecResponse: GatewayEndpoint.Response<T>, val runSentResult: RunSentResult)
