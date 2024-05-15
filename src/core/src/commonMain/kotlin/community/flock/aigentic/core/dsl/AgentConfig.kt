package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Context
import community.flock.aigentic.core.agent.Instruction
import community.flock.aigentic.core.agent.Task
import community.flock.aigentic.core.agent.message.DefaultSystemPromptBuilder
import community.flock.aigentic.core.agent.message.SystemPromptBuilder
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.tool.Tool

fun agent(agentConfig: AgentConfig.() -> Unit): Agent = AgentConfig().apply(agentConfig).build()

@AgentDSL
class AgentConfig : Config<Agent> {
    internal var model: Model? = null
    internal var id: String = "AgentId"
    internal var task: TaskConfig? = null
    internal var contexts: List<Context> = emptyList()
    internal var systemPromptBuilder: SystemPromptBuilder = DefaultSystemPromptBuilder

    internal val tools = mutableListOf<Tool>()

    fun AgentConfig.id(id: String) {
        this.id = id
    }

    fun AgentConfig.addTool(tool: Tool) = tools.add(tool)

    fun AgentConfig.context(contextConfig: ContextConfig.() -> Unit) = ContextConfig().apply(contextConfig).build().also { contexts = it }

    fun AgentConfig.task(
        description: String,
        taskConfig: TaskConfig.() -> Unit,
    ): TaskConfig =
        TaskConfig(description).apply(taskConfig)
            .also { task = it }

    fun AgentConfig.systemPrompt(systemPromptBuilder: SystemPromptBuilder) {
        this.systemPromptBuilder = systemPromptBuilder
    }

    fun AgentConfig.model(model: Model) {
        this.model = model
    }

    override fun build(): Agent =
        Agent(
            id = id,
            systemPromptBuilder = systemPromptBuilder,
            model = checkNotNull(model, builderPropertyMissingErrorMessage("model", "model()")),
            task = checkNotNull(task?.build(), builderPropertyMissingErrorMessage("task", "task()")),
            tools =
                check(
                    tools.isNotEmpty(),
                    builderPropertyMissingErrorMessage("tools", "addTool()"),
                ).let { tools.associateBy { it.name } },
            contexts = contexts,
        )
}

@AgentDSL
class TaskConfig(
    val description: String,
) : Config<Task> {
    internal val instructions = mutableListOf<Instruction>()

    fun TaskConfig.addInstruction(instruction: String) = instructions.add(Instruction(instruction))

    override fun build(): Task = Task(description, instructions)
}

@AgentDSL
class ContextConfig : Config<List<Context>> {
    internal val contexts = mutableListOf<Context>()

    fun ContextConfig.addText(text: String) =
        Context.Text(text)
            .also { contexts.add(it) }

    fun ContextConfig.addImage(base64: String) =
        Context.Image(base64)
            .also { contexts.add(it) }

    override fun build(): List<Context> = contexts
}
