package community.flock.aigentic.platform.client

import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.gateway.wirespec.GatewayEndpoint
import community.flock.aigentic.platform.mapper.toDto
import community.flock.aigentic.platform.util.createAgent
import community.flock.aigentic.platform.util.createRun
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.serializer

class AigenticPlatformEndpointsTest : DescribeSpec({

    it("should send http POST /gateway") {

        val agent = createAgent()
        val run = createRun()

        val mockEngine =
            MockEngine { request ->
                when (request.method to request.url.encodedPath) {
                    (HttpMethod.Post to "/gateway") ->
                        respond(content = ByteReadChannel.Empty, status = HttpStatusCode.Created)
                    else ->
                        error("Unexpected endpoint called! ${request.url.encodedPath}")
                }
            }

        val apiClient = AigenticPlatformEndpoints(Authentication.BasicAuth("", ""), PlatformApiUrl(""), mockEngine)

        val req = GatewayEndpoint.RequestApplicationJson(run.toDto(agent, serializer<String>()))
        val res = apiClient.gateway(req)

        res.status shouldBe HttpStatusCode.Created.value
    }
})
