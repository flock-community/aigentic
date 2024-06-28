package community.flock.aigentic.platform

import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.wirespec.GatewayEndpoint
import io.kotest.common.runBlocking
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.datetime.Clock

class PlatformClientTest : DescribeSpec({

    describe("push run") {
        println("Hello")

        val run = Run(
            startedAt = Clock.System.now(),
            finishedAt = Clock.System.now(),
            messages = emptyList(),
            modelRequests = emptyList(),
            result = Result.Finished(
                description = "description",
                response = "response"
            ),
        )

        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content = ByteReadChannel.Empty,
                    status = HttpStatusCode.Created,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            val apiClient = PlatformGatewayClient(mockEngine)



            val req = GatewayEndpoint.RequestApplicationJson(run.toDto())
            val res = apiClient.gateway(req)

            res.status shouldBe HttpStatusCode.OK.value
        }
    }
})
