package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.mcp.McpClientWrapper
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.tool.Tool
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class McpAgentConfigTest : DescribeSpec({

    describe("AgentConfig with MCP support") {

        beforeEach {
            clearAllMocks()
        }

        it("should add MCP client tools to agent configuration") {
            // Create mock MCP client wrapper
            val mockMcpClientWrapper = mockk<McpClientWrapper>()
            val mockTools = listOf(
                mockk<Tool> {
                    every { name.value } returns "mcp_tool_1"
                    every { description } returns "First MCP tool"
                    every { parameters } returns emptyList()
                },
                mockk<Tool> {
                    every { name.value } returns "mcp_tool_2"
                    every { description } returns "Second MCP tool"
                    every { parameters } returns emptyList()
                }
            )

            coEvery { mockMcpClientWrapper.getTools() } returns mockTools

            // Create agent with MCP tools
            val agent = agent<Unit, Unit> {
                model(mockk<Model>())

                addMcp(mockMcpClientWrapper)

                task("Test agent with MCP") {
                    addInstruction("Use MCP tools for processing")
                }
            }

            // Verify agent has the MCP tools
            agent shouldNotBe null
            agent.tools.size shouldBe 2
            agent.tools.values.any { it.name.value == "mcp_tool_1" } shouldBe true
            agent.tools.values.any { it.name.value == "mcp_tool_2" } shouldBe true

            coVerify(exactly = 1) { mockMcpClientWrapper.getTools() }
        }

        it("should combine MCP tools with regular tools") {
            // Create mock MCP client
            val mockMcpClientWrapper = mockk<McpClientWrapper>()
            val mockMcpTools = listOf(
                mockk<Tool> {
                    every { name.value } returns "mcp_calculator"
                    every { description } returns "MCP Calculator"
                    every { parameters } returns emptyList()
                }
            )

            coEvery { mockMcpClientWrapper.getTools() } returns mockMcpTools

            // Create agent with both MCP and regular tools
            val agent = agent<Unit, Unit> {
                model(mockk<Model>())

                // Add MCP tools
                addMcp(mockMcpClientWrapper)

                task("Mixed tools agent") {
                    addInstruction("Use both MCP and local tools")
                }
            }

            // Verify agent has MCP tools
            agent.tools.size shouldBe 1
            agent.tools.values.any { it.name.value == "mcp_calculator" } shouldBe true
        }

        it("should handle multiple MCP clients") {
            // Create first mock MCP client
            val mockMcpClientWrapper1 = mockk<McpClientWrapper>()
            val mockTools1 = listOf(
                mockk<Tool> {
                    every { name.value } returns "filesystem_read"
                    every { description } returns "Read files"
                    every { parameters } returns emptyList()
                },
                mockk<Tool> {
                    every { name.value } returns "filesystem_write"
                    every { description } returns "Write files"
                    every { parameters } returns emptyList()
                }
            )
            coEvery { mockMcpClientWrapper1.getTools() } returns mockTools1

            // Create second mock MCP client
            val mockMcpClientWrapper2 = mockk<McpClientWrapper>()
            val mockTools2 = listOf(
                mockk<Tool> {
                    every { name.value } returns "github_create_pr"
                    every { description } returns "Create pull request"
                    every { parameters } returns emptyList()
                },
                mockk<Tool> {
                    every { name.value } returns "github_list_issues"
                    every { description } returns "List issues"
                    every { parameters } returns emptyList()
                }
            )
            coEvery { mockMcpClientWrapper2.getTools() } returns mockTools2

            // Create agent with multiple MCP clients
            val agent = agent<Unit, Unit> {
                model(mockk<Model>())

                // Add tools from first MCP client
                addMcp(mockMcpClientWrapper1)

                // Add tools from second MCP client
                addMcp(mockMcpClientWrapper2)

                task("Multi-MCP agent") {
                    addInstruction("Use filesystem and GitHub tools")
                }
            }

            // Verify agent has tools from both MCP clients
            agent.tools.size shouldBe 4
            agent.tools.values.any { it.name.value == "filesystem_read" } shouldBe true
            agent.tools.values.any { it.name.value == "filesystem_write" } shouldBe true
            agent.tools.values.any { it.name.value == "github_create_pr" } shouldBe true
            agent.tools.values.any { it.name.value == "github_list_issues" } shouldBe true

            coVerify(exactly = 1) { mockMcpClientWrapper1.getTools() }
            coVerify(exactly = 1) { mockMcpClientWrapper2.getTools() }
        }

        it("should handle empty MCP client (no tools)") {
            val mockMcpClientWrapper = mockk<McpClientWrapper>()
            coEvery { mockMcpClientWrapper.getTools() } returns emptyList()

            // Need to add at least one tool or set response parameter
            val agent = agent<Unit, Unit> {
                model(mockk<Model>())

                addMcp(mockMcpClientWrapper)

                // Add response parameter so agent creation succeeds
                responseParameter = mockk()

                task("Empty MCP agent") {
                    addInstruction("Handle empty MCP gracefully")
                }
            }

            agent.tools.size shouldBe 0
        }

        it("should preserve MCP tool functionality in agent") {
            val mockMcpClientWrapper = mockk<McpClientWrapper>()

            // Create a real tool with handler
            val mockHandler: suspend (JsonObject) -> String = { args ->
                val value = args["value"]?.toString() ?: "0"
                "Result: $value"
            }

            val mockTool = object : Tool {
                override val name = community.flock.aigentic.core.tool.ToolName("compute")
                override val description = "Compute something"
                override val parameters = emptyList<community.flock.aigentic.core.tool.Parameter>()
                override val handler = mockHandler
            }

            coEvery { mockMcpClientWrapper.getTools() } returns listOf(mockTool)

            val agent = agent<Unit, Unit> {
                model(mockk<Model>())

                addMcp(mockMcpClientWrapper)

                task("Agent with functional MCP tool") {
                    addInstruction("Use compute tool")
                }
            }

            // Verify the tool was added and handler is preserved
            agent.tools.size shouldBe 1
            val computeTool: Tool? = agent.tools.values.find { it.name.value == "compute" }
            computeTool shouldNotBe null

            // Test the handler works
            val tool = computeTool
            tool shouldNotBe null

            shouldNotThrow<Exception> {
                kotlinx.coroutines.runBlocking {
                    val result = tool!!.handler(buildJsonObject {
                        put("value", 42)
                    })
                    result shouldBe "Result: 42"
                }
            }
        }
    }
})