package community.flock.aigentic.core.mcp

import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.ToolName
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.shared.RequestOptions
import kotlinx.serialization.json.*

class McpClientIntegrationTest : DescribeSpec({

    describe("McpClient Integration Tests") {

        describe("Tool Discovery") {

            it("should discover and convert MCP tools to Aigentic tools") {
                // Arrange
                val config = McpConfig.Stdio(
                    command = "echo",
                    args = listOf("test")
                )

                val mockClient = mockk<Client>()
                val toolList = listOf(
                    Tool(
                        name = "calculate",
                        description = "Performs calculations",
                        inputSchema = Tool.Input(
                            properties = buildJsonObject {
                                put("expression", buildJsonObject {
                                    put("type", "string")
                                    put("description", "Math expression to evaluate")
                                })
                            },
                            required = listOf("expression")
                        ),
                        outputSchema = null,
                        annotations = null
                    ),
                    Tool(
                        name = "search",
                        description = "Searches for information",
                        inputSchema = Tool.Input(
                            properties = buildJsonObject {
                                put("query", buildJsonObject {
                                    put("type", "string")
                                    put("description", "Search query")
                                })
                                put("limit", buildJsonObject {
                                    put("type", "integer")
                                    put("description", "Number of results")
                                })
                            },
                            required = listOf("query")
                        ),
                        outputSchema = null,
                        annotations = null
                    )
                )

                coEvery {
                    mockClient.listTools(any(), any())
                } returns ListToolsResult(tools = toolList, nextCursor = null)

                val mcpClient = McpClient.fromConfig(config)

                // Use reflection to inject mock client for testing
                val clientField = mcpClient::class.java.getDeclaredField("client")
                clientField.isAccessible = true
                clientField.set(mcpClient, mockClient)

                val initField = mcpClient::class.java.getDeclaredField("isInitialized")
                initField.isAccessible = true
                initField.set(mcpClient, true)

                // Act
                val tools = mcpClient.getTools()

                // Assert
                tools shouldHaveSize 2

                val calculateTool = tools[0]
                calculateTool.name shouldBe ToolName("calculate")
                calculateTool.description shouldBe "Performs calculations"
                calculateTool.parameters shouldHaveSize 1

                val expressionParam = calculateTool.parameters[0]
                expressionParam.shouldBeInstanceOf<Parameter.Primitive>()
                expressionParam.name shouldBe "expression"
                expressionParam.description shouldBe "Math expression to evaluate"
                expressionParam.isRequired shouldBe true

                val searchTool = tools[1]
                searchTool.name shouldBe ToolName("search")
                searchTool.description shouldBe "Searches for information"
                searchTool.parameters shouldHaveSize 2

                val queryParam = searchTool.parameters[0]
                queryParam.shouldBeInstanceOf<Parameter.Primitive>()
                queryParam.name shouldBe "query"
                queryParam.isRequired shouldBe true

                val limitParam = searchTool.parameters[1]
                limitParam.shouldBeInstanceOf<Parameter.Primitive>()
                limitParam.name shouldBe "limit"
                limitParam.isRequired shouldBe false
            }

            it("should handle empty tool list") {
                // Arrange
                val config = McpConfig.SSE(
                    url = "http://localhost:8080/mcp"
                )

                val mockClient = mockk<Client>()
                coEvery {
                    mockClient.listTools(any(), any())
                } returns ListToolsResult(tools = emptyList(), nextCursor = null)

                val mcpClient = McpClient.fromConfig(config)

                // Inject mock
                val clientField = mcpClient::class.java.getDeclaredField("client")
                clientField.isAccessible = true
                clientField.set(mcpClient, mockClient)

                val initField = mcpClient::class.java.getDeclaredField("isInitialized")
                initField.isAccessible = true
                initField.set(mcpClient, true)

                // Act
                val tools = mcpClient.getTools()

                // Assert
                tools.shouldBeEmpty()
            }

            it("should handle null tool list result") {
                // Arrange
                val config = McpConfig.WebSocket(
                    url = "ws://localhost:8080/mcp"
                )

                val mockClient = mockk<Client>()
                coEvery {
                    mockClient.listTools(any(), any())
                } returns null

                val mcpClient = McpClient.fromConfig(config)

                // Inject mock
                val clientField = mcpClient::class.java.getDeclaredField("client")
                clientField.isAccessible = true
                clientField.set(mcpClient, mockClient)

                val initField = mcpClient::class.java.getDeclaredField("isInitialized")
                initField.isAccessible = true
                initField.set(mcpClient, true)

                // Act
                val tools = mcpClient.getTools()

                // Assert
                tools.shouldBeEmpty()
            }
        }

        describe("Tool Execution") {

            it("should execute tool and return text result") {
                // Arrange
                val config = McpConfig.Stdio(
                    command = "test",
                    args = listOf()
                )

                val mockClient = mockk<Client>()
                val mcpTool = Tool(
                    name = "echo",
                    description = "Echoes input",
                    inputSchema = Tool.Input(
                        properties = buildJsonObject {
                            put("message", buildJsonObject {
                                put("type", "string")
                            })
                        },
                        required = listOf("message")
                    ),
                    outputSchema = null,
                    annotations = null
                )

                coEvery {
                    mockClient.listTools(any(), any())
                } returns ListToolsResult(tools = listOf(mcpTool), nextCursor = null)

                coEvery {
                    mockClient.callTool(
                        name = "echo",
                        arguments = mapOf("message" to "Hello"),
                        options = any()
                    )
                } returns CallToolResult(
                    content = listOf(TextContent(text = "Hello")),
                    structuredContent = null,
                    isError = false
                )

                val mcpClient = McpClient.fromConfig(config)

                // Inject mock
                val clientField = mcpClient::class.java.getDeclaredField("client")
                clientField.isAccessible = true
                clientField.set(mcpClient, mockClient)

                val initField = mcpClient::class.java.getDeclaredField("isInitialized")
                initField.isAccessible = true
                initField.set(mcpClient, true)

                // Act
                val tools = mcpClient.getTools()
                val echoTool = tools.first()

                val result = echoTool.handler(buildJsonObject {
                    put("message", "Hello")
                })

                // Assert
                result shouldBe "Hello"
            }

            it("should handle tool execution errors") {
                // Arrange
                val config = McpConfig.Stdio(
                    command = "test",
                    args = listOf()
                )

                val mockClient = mockk<Client>()
                val mcpTool = Tool(
                    name = "failing_tool",
                    description = "A tool that fails",
                    inputSchema = Tool.Input(),
                    outputSchema = null,
                    annotations = null
                )

                coEvery {
                    mockClient.listTools(any(), any())
                } returns ListToolsResult(tools = listOf(mcpTool), nextCursor = null)

                coEvery {
                    mockClient.callTool(
                        name = "failing_tool",
                        arguments = any(),
                        options = any()
                    )
                } returns CallToolResult(
                    content = listOf(TextContent(text = "Error occurred")),
                    structuredContent = null,
                    isError = true
                )

                val mcpClient = McpClient.fromConfig(config)

                // Inject mock
                val clientField = mcpClient::class.java.getDeclaredField("client")
                clientField.isAccessible = true
                clientField.set(mcpClient, mockClient)

                val initField = mcpClient::class.java.getDeclaredField("isInitialized")
                initField.isAccessible = true
                initField.set(mcpClient, true)

                // Act
                val tools = mcpClient.getTools()
                val failingTool = tools.first()

                val result = failingTool.handler(buildJsonObject {})

                // Assert
                result shouldBe "Error occurred"
            }

            it("should handle null tool result") {
                // Arrange
                val config = McpConfig.Stdio(
                    command = "test",
                    args = listOf()
                )

                val mockClient = mockk<Client>()
                val mcpTool = Tool(
                    name = "null_tool",
                    description = "Returns null",
                    inputSchema = Tool.Input(),
                    outputSchema = null,
                    annotations = null
                )

                coEvery {
                    mockClient.listTools(any(), any())
                } returns ListToolsResult(tools = listOf(mcpTool), nextCursor = null)

                coEvery {
                    mockClient.callTool(
                        name = "null_tool",
                        arguments = any(),
                        options = any()
                    )
                } returns null

                val mcpClient = McpClient.fromConfig(config)

                // Inject mock
                val clientField = mcpClient::class.java.getDeclaredField("client")
                clientField.isAccessible = true
                clientField.set(mcpClient, mockClient)

                val initField = mcpClient::class.java.getDeclaredField("isInitialized")
                initField.isAccessible = true
                initField.set(mcpClient, true)

                // Act
                val tools = mcpClient.getTools()
                val nullTool = tools.first()

                val result = nullTool.handler(buildJsonObject {})

                // Assert
                result shouldBe "No result from tool call"
            }
        }

        describe("Parameter Parsing") {

            it("should parse various parameter types correctly") {
                // Arrange
                val config = McpConfig.Stdio(
                    command = "test",
                    args = listOf()
                )

                val mockClient = mockk<Client>()
                val complexTool = Tool(
                    name = "complex",
                    description = "Complex tool with various parameter types",
                    inputSchema = Tool.Input(
                        properties = buildJsonObject {
                            put("stringParam", buildJsonObject {
                                put("type", "string")
                                put("description", "A string parameter")
                            })
                            put("numberParam", buildJsonObject {
                                put("type", "number")
                                put("description", "A number parameter")
                            })
                            put("integerParam", buildJsonObject {
                                put("type", "integer")
                                put("description", "An integer parameter")
                            })
                            put("booleanParam", buildJsonObject {
                                put("type", "boolean")
                                put("description", "A boolean parameter")
                            })
                            put("arrayParam", buildJsonObject {
                                put("type", "array")
                                put("description", "An array parameter")
                                put("items", buildJsonObject {
                                    put("type", "string")
                                })
                            })
                            put("enumParam", buildJsonObject {
                                put("type", "string")
                                put("description", "An enum parameter")
                                put("enum", buildJsonArray {
                                    add("option1")
                                    add("option2")
                                    add("option3")
                                })
                            })
                            put("objectParam", buildJsonObject {
                                put("type", "object")
                                put("description", "An object parameter")
                                put("properties", buildJsonObject {
                                    put("nested", buildJsonObject {
                                        put("type", "string")
                                    })
                                })
                                put("required", buildJsonArray {
                                    add("nested")
                                })
                            })
                        },
                        required = listOf("stringParam", "integerParam")
                    ),
                    outputSchema = null,
                    annotations = null
                )

                coEvery {
                    mockClient.listTools(any(), any())
                } returns ListToolsResult(tools = listOf(complexTool), nextCursor = null)

                val mcpClient = McpClient.fromConfig(config)

                // Inject mock
                val clientField = mcpClient::class.java.getDeclaredField("client")
                clientField.isAccessible = true
                clientField.set(mcpClient, mockClient)

                val initField = mcpClient::class.java.getDeclaredField("isInitialized")
                initField.isAccessible = true
                initField.set(mcpClient, true)

                // Act
                val tools = mcpClient.getTools()
                val tool = tools.first()

                // Assert
                tool.parameters shouldHaveSize 7

                // String parameter
                val stringParam = tool.parameters[0]
                stringParam.shouldBeInstanceOf<Parameter.Primitive>()
                stringParam.name shouldBe "stringParam"
                stringParam.isRequired shouldBe true
                stringParam.type shouldBe ParameterType.Primitive.String

                // Number parameter
                val numberParam = tool.parameters[1]
                numberParam.shouldBeInstanceOf<Parameter.Primitive>()
                numberParam.name shouldBe "numberParam"
                numberParam.isRequired shouldBe false
                numberParam.type shouldBe ParameterType.Primitive.Number

                // Integer parameter
                val integerParam = tool.parameters[2]
                integerParam.shouldBeInstanceOf<Parameter.Primitive>()
                integerParam.name shouldBe "integerParam"
                integerParam.isRequired shouldBe true
                integerParam.type shouldBe ParameterType.Primitive.Integer

                // Boolean parameter
                val boolParam = tool.parameters[3]
                boolParam.shouldBeInstanceOf<Parameter.Primitive>()
                boolParam.name shouldBe "booleanParam"
                boolParam.isRequired shouldBe false
                boolParam.type shouldBe ParameterType.Primitive.Boolean

                // Array parameter
                val arrayParam = tool.parameters[4]
                arrayParam.shouldBeInstanceOf<Parameter.Complex.Array>()
                arrayParam.name shouldBe "arrayParam"
                arrayParam.isRequired shouldBe false

                // Enum parameter
                val enumParam = tool.parameters[5]
                enumParam.shouldBeInstanceOf<Parameter.Complex.Enum>()
                enumParam.name shouldBe "enumParam"
                enumParam.isRequired shouldBe false
                enumParam.values shouldHaveSize 3

                // Object parameter
                val objectParam = tool.parameters[6]
                objectParam.shouldBeInstanceOf<Parameter.Complex.Object>()
                objectParam.name shouldBe "objectParam"
                objectParam.isRequired shouldBe false
                objectParam.parameters shouldHaveSize 1
            }
        }
    }
})