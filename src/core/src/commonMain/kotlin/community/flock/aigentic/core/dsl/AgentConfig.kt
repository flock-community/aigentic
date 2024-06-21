package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Context
import community.flock.aigentic.core.agent.Instruction
import community.flock.aigentic.core.agent.Platform
import community.flock.aigentic.core.agent.Task
import community.flock.aigentic.core.agent.message.DefaultSystemPromptBuilder
import community.flock.aigentic.core.agent.message.SystemPromptBuilder
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.Tool

fun agent(agentConfig: AgentConfig.() -> Unit): Agent = AgentConfig().apply(agentConfig).build()

@AgentDSL
class AgentConfig : Config<Agent> {
    internal var model: Model? = null
    internal var name: String? = null
    internal var platform: PlatformConfig? = null
    internal var task: TaskConfig? = null
    internal var contexts: List<Context> = emptyList()
    internal var systemPromptBuilder: SystemPromptBuilder = DefaultSystemPromptBuilder
    internal var responseParameter: Parameter? = null
    internal val tools = mutableListOf<Tool>()

    fun AgentConfig.name(name: String) {
        require(name.matches(Regex("^[a-zA-Z0-9_]*\$"))) {
            "Agent name can only contain letters, numbers and underscores"
        }
        this.name = name
    }

    fun AgentConfig.platform(platformConfig: PlatformConfig.() -> Unit) {
        PlatformConfig().apply(platformConfig).also { platform = it }
    }

    fun AgentConfig.addTool(tool: Tool) = tools.add(tool)

    fun AgentConfig.context(contextConfig: ContextConfig.() -> Unit) =
        ContextConfig().apply(contextConfig).build()
            .also { contexts = it }

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

    override fun build(): Agent =
        Agent(
            name = checkNotNull(name, builderPropertyMissingErrorMessage("name", "name()")),
            platform = platform?.build(),
            systemPromptBuilder = systemPromptBuilder,
            model = checkNotNull(model, builderPropertyMissingErrorMessage("model", "model()")),
            task = checkNotNull(task?.build(), builderPropertyMissingErrorMessage("task", "task()")),
            tools =
                check(
                    tools.isNotEmpty(),
                    builderPropertyMissingErrorMessage("tools", "addTool()"),
                ).let { tools.associateBy { it.name } },
            contexts = contexts,
            responseParameter = responseParameter,
        )
}

@AgentDSL
class PlatformConfig() : Config<Platform> {
    internal val secret: String? = null

    override fun build(): Platform =
        Platform(
            secret = checkNotNull(secret, builderPropertyMissingErrorMessage("secret", "platform { secret() }")),
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

    fun ContextConfig.addImageUrl(
        url: String,
        mimeType: MimeType,
    ) = Context.ImageUrl(url = url, mimeType = mimeType)
        .also { contexts.add(it) }

    fun ContextConfig.addImageBase64(
        base64: String,
        mimeType: MimeType,
    ) = Context.ImageBase64(base64 = base64, mimeType = mimeType)
        .also { contexts.add(it) }

    override fun build(): List<Context> = contexts
}
