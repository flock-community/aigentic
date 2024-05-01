package community.flock.aigentic.core.tool

import community.flock.aigentic.core.message.ToolCall
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline

interface Handler<T> {
    val handler: suspend (map: JsonObject) -> T
}

interface ToolDescription {
    val name: ToolName
    val description: String?
    val parameters: List<Parameter>
}

interface ToolConfigurationSupport {
    val toolConfiguration: ToolConfiguration
        get() = ToolConfiguration()
}

data class ToolConfiguration(val requestPermissionForAction: Boolean = false)

/**
 * Should not contain spaces (OpenAI returns 400 otherwise)
 */
@JvmInline
value class ToolName(val value: String)

interface Tool : ToolConfigurationSupport, ToolDescription, Handler<String>

internal interface InternalTool<T> : ToolDescription, Handler<T>

interface ToolPermissionHandler {
    suspend fun hasPermission(toolConfiguration: ToolConfiguration, toolCall: ToolCall): Boolean
}
