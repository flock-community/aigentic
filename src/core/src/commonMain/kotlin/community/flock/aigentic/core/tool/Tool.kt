package community.flock.aigentic.core.tool

import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline

interface ToolHandler<A, T> {
    val handler: suspend (toolArguments: A) -> T
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

interface Tool : ToolDescription, ToolHandler<JsonObject, String>

interface TypedTool<I, O> : ToolDescription, ToolHandler<I, O>

internal interface InternalTool<T> : ToolDescription, ToolHandler<JsonObject, T>
