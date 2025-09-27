package community.flock.aigentic.core.mcp

import community.flock.aigentic.core.tool.Tool

/**
 * Actual JVM implementation of MCP client wrapper
 */
actual class McpClientWrapper(private val mcpClient: McpClient) {
    actual suspend fun getTools(): List<Tool> = mcpClient.getTools()
}

/**
 * Actual JVM implementation to create MCP client from configuration
 */
actual fun createMcpClient(config: Any): McpClientWrapper {
    return when (config) {
        is McpConfig -> McpClientWrapper(McpClient.fromConfig(config))
        is McpClient -> McpClientWrapper(config)
        else -> throw IllegalArgumentException("Unsupported MCP configuration type: ${config::class}")
    }
}