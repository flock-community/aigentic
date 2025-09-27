package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.mcp.McpConfig
import community.flock.aigentic.core.mcp.McpConfigBuilder
import community.flock.aigentic.core.mcp.McpServers
import community.flock.aigentic.core.mcp.mcp

/**
 * JVM-specific MCP DSL extensions for AgentConfig
 */
fun <I : Any, O : Any> AgentConfig<I, O>.addMcp(mcpConfig: McpConfig) {
    addMcp(mcpConfig as Any)
}

fun <I : Any, O : Any> AgentConfig<I, O>.addMcp(block: McpConfigBuilder.() -> Unit) {
    val config = mcp(block)
    addMcp(config)
}