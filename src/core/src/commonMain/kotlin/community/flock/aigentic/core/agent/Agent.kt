package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.prompt.SystemPromptBuilder
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.tool.InternalTool
import community.flock.aigentic.core.tool.Tool
import kotlinx.coroutines.flow.MutableSharedFlow

data class Task(
    val description: String,
    val instructions: List<Instruction>
)

data class Instruction(val text: String)

sealed interface Context {
    data class Text(val text: String) : Context
    data class Image(val base64: String) : Context
}

data class Agent(
    val id: String,
    val systemPromptBuilder: SystemPromptBuilder,
    val model: Model,
    val task: Task,
    val contexts: List<Context>,
    val tools: Map<ToolName, Tool>,
) {

    internal val messages = MutableSharedFlow<Message>(replay = 100)
    internal val internalTools = mutableMapOf<ToolName, InternalTool<*>>()
}
