package community.flock.aigentic.core.mcp

import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.model.Model
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk

class McpClientSimpleTest : DescribeSpec({

    describe("McpClient - Simplified Implementation") {

        it("should create from stdio config") {
            val config = McpConfig.Stdio(
                command = "test",
                args = listOf("arg1"),
                name = "test-client",
                version = "1.0.0"
            )
            val mcpClient = McpClient.fromConfig(config)
            mcpClient shouldNotBe null
        }

        it("should support different config types") {
            val stdioConfig = McpConfig.Stdio("cmd", listOf("arg"))
            val sseConfig = McpConfig.SSE("http://example.com/sse")
            val wsConfig = McpConfig.WebSocket("ws://example.com/ws")

            val client1 = McpClient.fromConfig(stdioConfig)
            val client2 = McpClient.fromConfig(sseConfig)
            val client3 = McpClient.fromConfig(wsConfig)

            client1 shouldNotBe null
            client2 shouldNotBe null
            client3 shouldNotBe null
        }

        it("should return empty tools list (stub implementation)") {
            val config = McpConfig.Stdio("test", listOf())
            val mcpClient = McpClient.fromConfig(config)

            val tools = kotlinx.coroutines.runBlocking { mcpClient.getTools() }
            tools.size shouldBe 0
        }

        it("should work with agent configuration") {
            val config = McpConfig.Stdio("test", listOf())
            val mcpClient = McpClient.fromConfig(config)

            shouldNotThrow<Exception> {
                agent<Unit, Unit> {
                    model(mockk<Model>())

                    // Add MCP client (currently returns no tools)
                    val wrapper = McpClientWrapper(mcpClient)
                    addMcp(wrapper)

                    // Need at least one tool or response parameter for agent to be valid
                    responseParameter = mockk()

                    task("Test agent with MCP") {
                        addInstruction("Use MCP tools")
                    }
                }
            }
        }

        it("should support DSL configuration") {
            val config1 = mcp {
                stdio("npx", "-y", "@modelcontextprotocol/server-everything")
            }

            val config2 = mcp {
                sse("http://example.com/sse")
            }

            val config3 = mcp {
                websocket("ws://example.com/ws")
            }

            config1 shouldNotBe null
            config2 shouldNotBe null
            config3 shouldNotBe null

            (config1 is McpConfig.Stdio) shouldBe true
            (config2 is McpConfig.SSE) shouldBe true
            (config3 is McpConfig.WebSocket) shouldBe true
        }

        it("should provide convenient server configurations") {
            val fsConfig = McpServers.filesystem(listOf("/tmp", "/home"))
            val githubConfig = McpServers.github(token = "test-token")
            val npxConfig = McpServers.npx("some-package", listOf("arg1", "arg2"))

            (fsConfig is McpConfig.Stdio) shouldBe true
            (githubConfig is McpConfig.Stdio) shouldBe true
            (npxConfig is McpConfig.Stdio) shouldBe true

            fsConfig.command shouldBe "npx"
            githubConfig.env["GITHUB_TOKEN"] shouldBe "test-token"
        }
    }
})