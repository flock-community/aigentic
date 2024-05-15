package community.flock.aigentic.core.tool

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

/**
 * Should not contain spaces (OpenAI returns 400 otherwise)
 */
@JvmInline
value class ToolName(val value: String)

interface Tool : ToolDescription, Handler<String>

internal interface InternalTool<T> : ToolDescription, Handler<T>
