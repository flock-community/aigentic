package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.message.SystemPromptBuilder
import community.flock.aigentic.core.agent.status.toStatus
import community.flock.aigentic.core.agent.tool.finishOrStuckTool
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.tool.InternalTool
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapConcat

data class Task(
    val description: String,
    val instructions: List<Instruction>,
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
    internal val internalTools: Map<ToolName, InternalTool<*>> = mapOf(finishOrStuckTool.name to finishOrStuckTool)
    internal val messages = MutableSharedFlow<Message>(replay = 100)
}

fun Agent.getMessages() = messages.asSharedFlow()

fun Agent.getStatus() = messages.flatMapConcat { it.toStatus().asFlow() }
