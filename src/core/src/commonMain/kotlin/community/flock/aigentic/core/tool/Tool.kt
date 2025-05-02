package community.flock.aigentic.core.tool

import community.flock.aigentic.core.dsl.AgentConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
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

interface TypedTool<I : Any, O : Any> : ToolDescription, ToolHandler<I, O>

internal interface InternalTool<T> : ToolDescription, ToolHandler<JsonObject, T>


inline fun <reified I : Any, reified O : Any> TypedTool<I, O>.toTool(): Tool = object : Tool {
    override val name: ToolName = this@toTool.name
    override val description: String? = this@toTool.description
    override val parameters: List<Parameter>
        get() {
            val parameter = getParameter<I>()
                ?: error("No parameter found for type ${I::class.simpleName}. Make sure the class has @AigenticParameter annotation and Aigentic.initialize() has been called.")
            return parameter.parameters
        }
    override val handler: suspend (toolArguments: JsonObject) -> String = {
        val obj = Json.decodeFromJsonElement<I>(it)
        val res = this@toTool.handler(obj)
        Json.encodeToString<O>(res)
    }
}

inline fun <reified I : Any, reified O : Any> AgentConfig.toTool(
    name: String,
    description: String? = null,
    noinline handler: suspend (I) -> O,
): Tool = object : Tool {
    override val name: ToolName = ToolName(name)
    override val description: String? = description
    override val parameters: List<Parameter>
        get() {
            val parameter = getParameter<I>()
                ?: error("No parameter found for type ${I::class.simpleName}. Make sure the class has @AigenticParameter annotation and Aigentic.initialize() has been called.")
            return parameter.parameters
        }
    override val handler: suspend (toolArguments: JsonObject) -> String = {
        val obj = Json.decodeFromJsonElement<I>(it)
        val res = handler(obj)
        Json.encodeToString<O>(res)
    }
}
