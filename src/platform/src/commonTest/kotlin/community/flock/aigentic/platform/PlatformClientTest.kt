package community.flock.aigentic.platform

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Instruction
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.Task
import community.flock.aigentic.core.agent.message.DefaultSystemPromptBuilder
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.platform.Authentication
import community.flock.aigentic.core.platform.PlatformApiUrl
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.platform.client.PlatformGatewayClient
import community.flock.aigentic.gateway.wirespec.GatewayEndpoint
import community.flock.aigentic.platform.mapper.toDto
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import kotlinx.datetime.Clock

class PlatformClientTest : DescribeSpec({

    it("should send http POST /gateway") {

        val agent = Agent(
            name = "test-agent",
            platform = null,
            systemPromptBuilder = DefaultSystemPromptBuilder,
            model = object: Model {
                override val authentication: community.flock.aigentic.core.model.Authentication
                    get() = TODO("Not yet implemented")
                override val modelIdentifier: ModelIdentifier = object: ModelIdentifier {
                    override val stringValue: String = "test-model-identifier"
                }

                override suspend fun sendRequest(messages: List<Message>, tools: List<ToolDescription>): ModelResponse {
                    TODO("Not yet implemented")
                }

            },
            task = Task(
                description = "description",
                instructions = listOf(Instruction("Some instruction")),
            ),
            contexts = emptyList(),
            tools = emptyMap()
        )

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

        val apiClient = PlatformGatewayClient(Authentication.BasicAuth("", ""), PlatformApiUrl(""), mockEngine)

        val req = GatewayEndpoint.RequestApplicationJson(run.toDto(agent))
        val res = apiClient.gateway(req)

        res.status shouldBe HttpStatusCode.Created.value
    }
})
