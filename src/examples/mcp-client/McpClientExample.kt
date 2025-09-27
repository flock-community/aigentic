package community.flock.aigentic.example

import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.mcp.McpConfig
import community.flock.aigentic.core.mcp.McpServers
import community.flock.aigentic.core.model.Model
import kotlinx.coroutines.runBlocking

/**
 * Example of using MCP Client with Aigentic
 *
 * The new cleaner API automatically handles:
 * - Initialization and connection to MCP servers
 * - Tool discovery and registration
 * - Lazy initialization when tools are first used
 */
object McpClientExample {

    /**
     * Example 1: Simple MCP integration with inline DSL
     */
    fun createAgentWithInlineMcp() {
        val agent = agent<String, String> {
            model(Model.GPT_4)

            // Add MCP server using inline DSL - connection happens automatically
            addMcp {
                stdio(
                    command = "npx",
                    args = arrayOf("-y", "@modelcontextprotocol/server-everything")
                )
            }

            // Add regular tools alongside MCP tools
            addTool<String, String>("local_tool", "A local tool") { input ->
                "Processed locally: $input"
            }

            task("Assistant with MCP tools") {
                addInstruction("Use the available MCP tools to help answer questions")
            }
        }

        println("✅ Agent created with MCP tools using inline DSL!")
    }

    /**
     * Example 2: Using predefined MCP server configurations
     */
    fun createAgentWithPredefinedServers() {
        val agent = agent<String, String> {
            model(Model.GPT_4)

            // Use predefined server configurations
            addMcp(McpServers.filesystem(
                allowedDirectories = listOf("/tmp", "/home/user/documents")
            ))

            // GitHub server with token from environment
            val githubToken = System.getenv("GITHUB_TOKEN")
            if (githubToken != null) {
                addMcp(McpServers.github(token = githubToken))
            }

            // Postgres server
            addMcp(McpServers.postgres(
                connectionString = "postgresql://localhost:5432/mydb"
            ))

            task("Assistant with filesystem, GitHub, and database access") {
                addInstruction("Use filesystem tools for local file operations")
                addInstruction("Use GitHub tools for repository operations")
                addInstruction("Use Postgres tools for database queries")
            }
        }

        println("✅ Agent created with predefined MCP servers!")
    }

    /**
     * Example 3: Using custom MCP configurations
     */
    fun createAgentWithCustomConfigs() {
        val agent = agent<String, String> {
            model(Model.GPT_4)

            // Custom stdio configuration
            addMcp(
                McpConfig.Stdio(
                    command = "python",
                    args = listOf("my_mcp_server.py"),
                    env = mapOf("API_KEY" to "secret"),
                    name = "custom-python-server"
                )
            )

            // SSE configuration
            addMcp(
                McpConfig.SSE(
                    url = "https://mcp-server.example.com/sse",
                    headers = mapOf("Authorization" to "Bearer token"),
                    name = "remote-sse-server"
                )
            )

            // WebSocket configuration
            addMcp(
                McpConfig.WebSocket(
                    url = "wss://mcp-server.example.com/ws",
                    name = "remote-ws-server"
                )
            )

            task("Assistant with multiple custom MCP servers") {
                addInstruction("Use tools from all connected MCP servers")
            }
        }

        println("✅ Agent created with custom MCP configurations!")
    }

    /**
     * Example 4: Multiple MCP servers with different transports
     */
    fun createMultiTransportAgent() {
        val agent = agent<String, String> {
            model(Model.GPT_4)

            // Mix different configuration styles

            // 1. Inline DSL for stdio
            addMcp {
                stdio("npx", "-y", "@modelcontextprotocol/server-filesystem", "/tmp")
            }

            // 2. Predefined server
            addMcp(McpServers.slack(token = System.getenv("SLACK_TOKEN") ?: ""))

            // 3. Custom SSE server
            addMcp {
                sse(
                    url = "https://api.example.com/mcp/events",
                    headers = mapOf("X-API-Key" to "12345")
                )
            }

            // 4. Custom WebSocket server
            addMcp {
                websocket("wss://realtime.example.com/mcp")
            }

            task("Multi-transport assistant") {
                addInstruction("Coordinate between filesystem, Slack, and remote APIs")
            }
        }

        println("✅ Agent created with multiple transports!")
    }

    /**
     * Example 5: Minimal configuration
     */
    fun createMinimalAgent() {
        // Super clean, minimal configuration
        val agent = agent<String, String> {
            model(Model.GPT_4)

            // Just one line to add an MCP server!
            addMcp(McpServers.npx("@modelcontextprotocol/server-everything"))

            task("Minimal MCP assistant") {
                addInstruction("Help with general tasks")
            }
        }

        println("✅ Minimal agent created with one-line MCP configuration!")
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        println("=== MCP Client Examples - Clean API ===\n")

        try {
            println("1. Inline DSL:")
            createAgentWithInlineMcp()
            println()

            println("2. Predefined servers:")
            createAgentWithPredefinedServers()
            println()

            println("3. Custom configurations:")
            createAgentWithCustomConfigs()
            println()

            println("4. Multiple transports:")
            createMultiTransportAgent()
            println()

            println("5. Minimal configuration:")
            createMinimalAgent()
            println()

            println("All agents created successfully! MCP connections will be established lazily when tools are first used.")

        } catch (e: Exception) {
            println("Error: ${e.message}")
            e.printStackTrace()
        }
    }
}