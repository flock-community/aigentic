package community.flock.aigentic.platform

import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.wirespec.GatewayEndpoint
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import kotlinx.datetime.Clock

class PlatformClientTest : DescribeSpec({

    it("should send http POST /gatway") {
        val run =
            Run(
                startedAt = Clock.System.now(),
                finishedAt = Clock.System.now(),
                messages = emptyList(),
                modelRequests = emptyList(),
                result =
                    Result.Finished(
                        description = "description",
                        response = "response",
                    ),
            )

        val mockEngine =
            MockEngine { request ->
                when (request.method to request.url.encodedPath) {
                    (HttpMethod.Post to "/gateway") -> respond(content = ByteReadChannel.Empty, status = HttpStatusCode.Created)
                    else -> error("Unhandled ${request.url.encodedPath}")
                }
            }

        val apiClient = PlatformGatewayClient(mockEngine)

        val req = GatewayEndpoint.RequestApplicationJson(run.toDto())
        val res = apiClient.gateway(req)

        res.status shouldBe HttpStatusCode.Created.value
    }
})
