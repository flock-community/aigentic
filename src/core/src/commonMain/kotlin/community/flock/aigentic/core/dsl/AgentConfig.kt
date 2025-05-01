package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Context
import community.flock.aigentic.core.agent.Instruction
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.agent.Task
import community.flock.aigentic.core.agent.message.DefaultSystemPromptBuilder
import community.flock.aigentic.core.agent.message.SystemPromptBuilder
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.Tool

fun agent(agentConfig: AgentConfig.() -> Unit): Agent = AgentConfig().apply(agentConfig).build()

@AgentDSL
class AgentConfig : Config<Agent> {
    internal var model: Model? = null
    internal var platform: Platform? = null
    internal var task: TaskConfig? = null
    internal var systemPromptBuilder: SystemPromptBuilder = DefaultSystemPromptBuilder
    internal var responseParameter: Parameter? = null
    internal val tools = mutableListOf<Tool>()
    internal val tags = mutableListOf<RunTag>()

    fun AgentConfig.platform(platform: Platform) {
        this.platform = platform
    }

    fun AgentConfig.addTool(tool: Tool) = tools.add(tool)

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

    fun AgentConfig.finishResponse(response: Parameter) {
        this.responseParameter = response
    }

    fun AgentConfig.tags(tag: String) = tags.add(RunTag(tag))

    override fun build(): Agent =
        Agent(
            platform = platform,
            systemPromptBuilder = systemPromptBuilder,
            model = checkNotNull(model, builderPropertyMissingErrorMessage("model", "model()")),
            task = checkNotNull(task?.build(), builderPropertyMissingErrorMessage("task", "task()")),
            tools =
                check(
                    tools.isNotEmpty() || responseParameter != null,
                    builderPropertyMissingErrorMessage("tools", "addTool()"),
                ).let { tools.associateBy { it.name } },
            contexts = emptyList(),
            responseParameter = responseParameter,
            tags = tags,
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
class GenerationConfig : Config<GenerationSettings> {
    internal var temperature: Float = GenerationSettings.DEFAULT_TEMPERATURE
    internal var topK: Int = GenerationSettings.DEFAULT_TOP_K
    internal var topP: Float = GenerationSettings.DEFAULT_TOP_P

    fun GenerationConfig.temperature(temperature: Float) {
        this.temperature = temperature
    }

    fun GenerationConfig.topK(topK: Int) {
        this.topK = topK
    }

    fun GenerationConfig.topP(topP: Float) {
        this.topP = topP
    }

    override fun build(): GenerationSettings =
        GenerationSettings(
            temperature = temperature,
            topK = topK,
            topP = topP,
        )
}
