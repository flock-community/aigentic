package community.flock.aigentic.platform.client

import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.core.platform.RunSentResult
import community.flock.aigentic.gateway.wirespec.endpoint.Gateway
import community.flock.aigentic.gateway.wirespec.model.GatewayClientErrorDto
import community.flock.aigentic.gateway.wirespec.model.ServerErrorDto
import community.flock.aigentic.platform.util.createAgent
import community.flock.aigentic.platform.util.createAgentRun
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.serializer

class AigenticPlatformClientTest : DescribeSpec({

    withData(
        nameFn = { "Should map ${it.wirespecResponse} to ${it.runSentResult}" },
        TestCase(Gateway.Response201(body = Unit), RunSentResult.Success),
        TestCase(Gateway.Response401(body = Unit), RunSentResult.Unauthorized),
        TestCase(
            Gateway.Response400(body = GatewayClientErrorDto("invalid request")),
            RunSentResult.Error("invalid request"),
        ),
        TestCase(
            Gateway.Response500(body = ServerErrorDto("error", "something went wrong")),
            RunSentResult.Error("error - something went wrong"),
        ),
    ) {

        val agent = createAgent()
        val run = createAgentRun()

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

        val result = client.sendRun(run, agent, serializer<String>())

        result shouldBe it.runSentResult
    }
})

private data class TestCase<T : Any>(val wirespecResponse: Gateway.Response<T>, val runSentResult: RunSentResult)
