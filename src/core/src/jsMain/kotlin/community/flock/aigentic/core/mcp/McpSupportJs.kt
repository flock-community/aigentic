package community.flock.aigentic.core.mcp

import community.flock.aigentic.core.tool.Tool

/**
 * JS implementation of MCP client wrapper
 * MCP is not yet supported on JS platform
 */
actual class McpClientWrapper {
    actual suspend fun getTools(): List<Tool> {
        throw UnsupportedOperationException("MCP is not supported on JS platform yet")
    }
}

/**
 * JS implementation to create MCP client from configuration
 * MCP is not yet supported on JS platform
 */
actual fun createMcpClient(config: Any): McpClientWrapper {
    throw UnsupportedOperationException("MCP is not supported on JS platform yet")
}