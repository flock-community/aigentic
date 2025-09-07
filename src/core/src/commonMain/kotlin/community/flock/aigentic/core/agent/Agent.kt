package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.message.AgenticSystemPromptBuilder
import community.flock.aigentic.core.agent.message.StructuredOutputPromptBuilder
import community.flock.aigentic.core.agent.message.SystemPromptBuilder
import community.flock.aigentic.core.agent.tool.finishedTaskTool
import community.flock.aigentic.core.agent.tool.stuckWithTaskTool
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.tool.InternalTool
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName

data class Task(
    val description: String,
    val instructions: List<Instruction>,
)

data class Instruction(val text: String)

sealed interface Context {
    data class Text(val text: String) : Context

    data class Url(val url: String, val mimeType: MimeType) : Context

    data class Base64(val base64: String, val mimeType: MimeType) : Context
}

data class Agent<I : Any, O : Any>(
    val platform: Platform?,
    val customSystemPromptBuilder: SystemPromptBuilder?,
    val model: Model,
    val task: Task,
    val contexts: List<Context>,
    val tools: Map<ToolName, Tool>,
    val responseParameter: Parameter? = null,
    val tags: List<RunTag>,
) {
    @PublishedApi
    internal inline fun <reified TO : Any> finishedTaskTool() = finishedTaskTool<TO>(responseParameter)

    @PublishedApi
    internal inline fun <reified TO : Any> internalTools(): Map<ToolName, InternalTool<*>> =
        listOf(
            finishedTaskTool<TO>(),
            stuckWithTaskTool,
        ).associateBy { it.name }

    fun getSystemPromptMessage(): Message.SystemPrompt {
        val systemPromptBuilder =
            when {
                customSystemPromptBuilder != null -> customSystemPromptBuilder
                isStructuredOutputAgent() -> StructuredOutputPromptBuilder
                else -> AgenticSystemPromptBuilder
            }
        return systemPromptBuilder.buildSystemPrompt(this)
    }
}

fun <I : Any, O : Any> Agent<I, O>.isStructuredOutputAgent(): Boolean = tools.isEmpty() && responseParameter != null
