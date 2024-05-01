package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.prompt.SystemPromptBuilder
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.tool.InternalTool
import community.flock.aigentic.core.tool.Tool
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Task(
    val description: String,
    val instructions: List<Instruction>
)

data class Instruction(val text: String)

sealed interface Context {
    data class Text(val text: String) : Context
    data class Image(val base64: String) : Context
}

enum class AgentRunningState(val value: String) {
    WAITING_TO_START("WAITING_TO_START"),
    RUNNING("RUNNING"),
    EXECUTING_TOOL("EXECUTING_TOOL"),
    WAITING_ON_APPROVAL("WAITING_ON_APPROVAL"),
    COMPLETED("COMPLETED"),
    STUCK("STUCK"),
}

data class AgentStatus(
    var runningState: AgentRunningState = AgentRunningState.WAITING_TO_START,
    val startTimestamp: Instant = Clock.System.now(),
    var endTimestamp: Instant? = null,
)

data class Agent(
    val id: String,
    val systemPromptBuilder: SystemPromptBuilder,
    val model: Model,
    val task: Task,
    val contexts: List<Context>,
    val tools: Map<ToolName, Tool>,
) {
    val messages = MutableSharedFlow<Message>(replay = 100)
    internal val status = MutableStateFlow(AgentStatus())
    internal val internalTools = mutableMapOf<ToolName, InternalTool<*>>()
}