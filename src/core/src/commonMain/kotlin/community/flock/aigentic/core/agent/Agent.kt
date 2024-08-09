package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.message.SystemPromptBuilder
import community.flock.aigentic.core.agent.tool.finishedTaskTool
import community.flock.aigentic.core.agent.tool.stuckWithTaskTool
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

data class Agent(
    val platform: Platform?,
    val systemPromptBuilder: SystemPromptBuilder,
    val model: Model,
    val task: Task,
    val contexts: List<Context>,
    val tools: Map<ToolName, Tool>,
    val responseParameter: Parameter? = null,
) {
    internal val finishedTaskTool = finishedTaskTool(responseParameter)
    internal val internalTools: Map<ToolName, InternalTool<*>> =
        listOf(
            finishedTaskTool,
            stuckWithTaskTool,
        ).associateBy { it.name }
}
