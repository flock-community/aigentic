package community.flock.aigentic.core.mcp

import community.flock.aigentic.core.tool.Tool

/**
 * Expected MCP support interface for multiplatform
 * Actual implementations are platform-specific
 */
expect class McpClientWrapper {
    suspend fun getTools(): List<Tool>
}

/**
 * Create an MCP client wrapper from configuration
 * Platform-specific implementations will handle the actual MCP SDK
 */
expect fun createMcpClient(config: Any): McpClientWrapper