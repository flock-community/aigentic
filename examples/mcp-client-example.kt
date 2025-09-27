#!/usr/bin/env kotlin

/**
 * Example demonstrating MCP (Model Context Protocol) client integration with Aigentic
 *
 * This example shows how to:
 * 1. Create MCP clients with various transport configurations
 * 2. Add MCP servers as tool sources to agents
 * 3. Use predefined MCP server configurations
 * 4. Execute tools from MCP servers through agents
 */

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.execute
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.dsl.config
import community.flock.aigentic.core.dsl.prompt
import community.flock.aigentic.core.mcp.*
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.openai.gptModel
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("=== MCP Client Integration Example ===\n")

    // Example 1: Using predefined MCP server configurations
    println("Example 1: Using Predefined MCP Server Configurations")
    println("------------------------------------------------------")

    val filesystemAgent = agent {
        model = gptModel("gpt-4") // Replace with your model

        // Add filesystem MCP server with access to /tmp
        addMcp(McpServers.filesystem(
            allowedDirectories = listOf("/tmp", "/home/user/documents")
        ))

        prompt {
            system("You are a helpful assistant with file system access.")
        }
    }

    println("Created agent with filesystem MCP server")
    println("Available tools: ${filesystemAgent.tools.keys}")
    println()

    // Example 2: Using custom STDIO MCP server
    println("Example 2: Custom STDIO MCP Server")
    println("-----------------------------------")

    val customStdioConfig = McpConfig.Stdio(
        command = "node",
        args = listOf("/path/to/your/mcp-server.js"),
        env = mapOf(
            "API_KEY" to "your-api-key",
            "ENV" to "production"
        ),
        name = "custom-mcp-server"
    )

    val customAgent = agent {
        model = gptModel("gpt-4")

        // Add custom STDIO MCP server
        addMcp(customStdioConfig)

        prompt {
            system("You are an agent with access to custom MCP tools.")
        }
    }

    println("Created agent with custom STDIO MCP server")
    println()

    // Example 3: Using SSE (Server-Sent Events) MCP server
    println("Example 3: SSE MCP Server")
    println("-------------------------")

    val sseConfig = McpConfig.SSE(
        url = "http://localhost:8080/mcp/sse",
        headers = mapOf(
            "Authorization" to "Bearer your-token",
            "X-Client-Version" to "1.0.0"
        )
    )

    val sseAgent = agent {
        model = gptModel("gpt-4")

        // Add SSE MCP server
        addMcp(sseConfig)

        prompt {
            system("You are connected to an SSE-based MCP server.")
        }
    }

    println("Created agent with SSE MCP server")
    println()

    // Example 4: Using WebSocket MCP server
    println("Example 4: WebSocket MCP Server")
    println("--------------------------------")

    val wsConfig = McpConfig.WebSocket(
        url = "ws://localhost:8080/mcp/ws",
        headers = mapOf(
            "Authorization" to "Bearer your-token"
        )
    )

    val wsAgent = agent {
        model = gptModel("gpt-4")

        // Add WebSocket MCP server
        addMcp(wsConfig)

        prompt {
            system("You are connected to a WebSocket-based MCP server.")
        }
    }

    println("Created agent with WebSocket MCP server")
    println()

    // Example 5: Using MCP DSL for configuration
    println("Example 5: MCP DSL Configuration")
    println("---------------------------------")

    val dslConfig = mcp {
        // Choose one transport type:

        // Option 1: STDIO
        stdio(
            command = "npx",
            "-y", "@modelcontextprotocol/server-everything",
            env = mapOf("DEBUG" to "true"),
            name = "everything-server"
        )

        // Option 2: SSE (uncomment to use)
        // sse(
        //     url = "http://localhost:3000/mcp",
        //     headers = mapOf("X-API-Key" to "secret")
        // )

        // Option 3: WebSocket (uncomment to use)
        // websocket(
        //     url = "ws://localhost:3000/mcp",
        //     headers = mapOf("X-API-Key" to "secret")
        // )
    }

    val dslAgent = agent {
        model = gptModel("gpt-4")
        addMcp(dslConfig)

        prompt {
            system("You have access to the everything MCP server.")
        }
    }

    println("Created agent with MCP DSL configuration")
    println()

    // Example 6: Using multiple MCP servers in one agent
    println("Example 6: Multiple MCP Servers")
    println("--------------------------------")

    val multiMcpAgent = agent {
        model = gptModel("gpt-4")

        // Add multiple MCP servers
        addMcp(McpServers.filesystem(allowedDirectories = listOf("/tmp")))
        addMcp(McpServers.github(token = System.getenv("GITHUB_TOKEN")))

        // You can also add custom servers
        addMcp(mcp {
            stdio("node", "/path/to/custom-server.js")
        })

        prompt {
            system("""
                You are a powerful assistant with access to:
                1. File system operations
                2. GitHub API
                3. Custom server capabilities

                Use these tools to help the user effectively.
            """.trimIndent())
        }
    }

    println("Created agent with multiple MCP servers")
    println("Total available tools: ${multiMcpAgent.tools.size}")
    println()

    // Example 7: Executing a task with MCP tools
    println("Example 7: Task Execution")
    println("-------------------------")

    // Note: This is a demonstration. In a real scenario, ensure MCP servers are running
    try {
        val testAgent = agent {
            model = gptModel("gpt-4")

            // For testing, let's use a simple configuration
            addMcp(McpServers.filesystem(allowedDirectories = listOf("/tmp")))

            prompt {
                system("You are a file system assistant. Help users with file operations.")
            }
        }

        // Execute a task (this would work if the MCP server is running)
        val result = testAgent.execute("List the files in /tmp directory")

        println("Task execution result:")
        println(result)
    } catch (e: Exception) {
        println("Note: Task execution failed (expected if MCP server is not running)")
        println("Error: ${e.message}")
    }

    println("\n=== Example Complete ===")

    // Additional notes
    println("""

        Notes for running this example:
        ------------------------------------------------
        1. Install MCP servers before running:
           - Filesystem: npx @modelcontextprotocol/server-filesystem
           - GitHub: npx @modelcontextprotocol/server-github
           - Others: Check MCP server documentation

        2. Set environment variables as needed:
           - GITHUB_TOKEN for GitHub server
           - API keys for custom servers

        3. MCP servers must be accessible from your application:
           - STDIO servers are spawned as child processes
           - SSE/WebSocket servers must be running separately

        4. The MCP client will lazily initialize when first used

        5. All MCP tools are automatically discovered and made available to the agent
    """.trimIndent())
}