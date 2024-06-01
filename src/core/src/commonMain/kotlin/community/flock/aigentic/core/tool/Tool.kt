package community.flock.aigentic.core.tool

import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline

interface ToolHandler<T> {
    val handler: suspend (toolArguments: JsonObject) -> T
}

interface ToolDescription {
    val name: ToolName
    val description: String?
    val parameters: List<Parameter>
}

/**
 * Should not contain spaces (OpenAI returns 400 otherwise)
 */
@JvmInline
value class ToolName(val value: String)

interface Tool : ToolDescription, ToolHandler<String>

internal interface InternalTool<T> : ToolDescription, ToolHandler<T>
