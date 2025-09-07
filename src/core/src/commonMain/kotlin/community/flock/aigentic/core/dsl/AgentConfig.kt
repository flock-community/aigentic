package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Context
import community.flock.aigentic.core.agent.Instruction
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.agent.Task
import community.flock.aigentic.core.agent.message.SystemPromptBuilder
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ThinkingConfig
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.TypedTool
import community.flock.aigentic.core.tool.createTool
import community.flock.aigentic.core.tool.getParameter
import kotlin.jvm.JvmName

@JvmName("agentDefault")
fun agent(agentConfig: AgentConfig<Unit, Unit>.() -> Unit): Agent<Unit, Unit> = AgentConfig<Unit, Unit>().apply(agentConfig).build()

@JvmName("agentInput")
fun <I : Any> agent(agentConfig: AgentConfig<I, Unit>.() -> Unit): Agent<I, Unit> = AgentConfig<I, Unit>().apply(agentConfig).build()

inline fun <I : Any, reified O : Any> agent(agentConfig: AgentConfig<I, O>.() -> Unit): Agent<I, O> =
    AgentConfig<I, O>()
        .apply {
            if (O::class != Unit::class) {
                setFinishResponse<O>()
            }
        }
        .apply(agentConfig)
        .build()

@AgentDSL
class AgentConfig<I : Any, O : Any> : Config<Agent<I, O>> {
    internal var model: Model? = null
    internal var platform: Platform? = null
    internal var task: TaskConfig? = null
    internal var contexts: List<Context> = emptyList()
    internal var customSystemPromptBuilder: SystemPromptBuilder? = null
    var responseParameter: Parameter? = null
    val tools = mutableListOf<Tool>()
    internal val tags = mutableListOf<RunTag>()

    fun AgentConfig<I, O>.platform(platform: Platform) {
        this.platform = platform
    }

    fun AgentConfig<I, O>.addTool(tool: Tool) {
        tools += tool
    }

    inline fun <reified TI : Any, reified TO : Any> AgentConfig<I, O>.addTool(tool: TypedTool<TI, TO>) {
        tools += tool.createTool()
    }

    inline fun <reified TI : Any, reified TO : Any> AgentConfig<I, O>.addTool(
        name: String,
        description: String? = null,
        noinline handler: suspend (TI) -> TO,
    ) {
        tools += createTool(name, description, handler)
    }

    fun AgentConfig<I, O>.context(contextConfig: ContextConfig.() -> Unit) =
        ContextConfig().apply(contextConfig).build()
            .also { contexts = it }

    fun AgentConfig<I, O>.task(
        description: String,
        taskConfig: TaskConfig.() -> Unit,
    ): TaskConfig =
        TaskConfig(description).apply(taskConfig)
            .also { task = it }

    fun AgentConfig<I, O>.systemPrompt(systemPromptBuilder: SystemPromptBuilder) {
        this.customSystemPromptBuilder = systemPromptBuilder
    }

    fun AgentConfig<I, O>.model(model: Model) {
        this.model = model
    }

    @PublishedApi
    internal inline fun <reified O : Any> AgentConfig<I, O>.setFinishResponse() {
        this.responseParameter = getParameter<O>()
    }

    fun AgentConfig<I, O>.tags(tag: String) = tags.add(RunTag(tag))

    override fun build(): Agent<I, O> =
        Agent(
            platform = platform,
            customSystemPromptBuilder = customSystemPromptBuilder,
            model = checkNotNull(model, builderPropertyMissingErrorMessage("model", "model()")),
            task = checkNotNull(task?.build(), builderPropertyMissingErrorMessage("task", "task()")),
            tools =
                check(
                    tools.isNotEmpty() || responseParameter != null,
                    builderPropertyMissingErrorMessage("tools", "addTool()"),
                ).let { tools.associateBy { it.name } },
            contexts = contexts,
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
class ContextConfig : Config<List<Context>> {
    internal val contexts = mutableListOf<Context>()

    fun ContextConfig.addText(text: String) =
        Context.Text(text)
            .also { contexts.add(it) }

    fun ContextConfig.addUrl(
        url: String,
        mimeType: MimeType,
    ) = Context.Url(url = url, mimeType = mimeType)
        .also { contexts.add(it) }

    fun ContextConfig.addBase64(
        base64: String,
        mimeType: MimeType,
    ) = Context.Base64(base64 = base64, mimeType = mimeType)
        .also { contexts.add(it) }

    override fun build(): List<Context> = contexts
}

@AgentDSL
class GenerationConfig : Config<GenerationSettings> {
    internal var temperature: Float = GenerationSettings.DEFAULT_TEMPERATURE
    internal var topK: Int = GenerationSettings.DEFAULT_TOP_K
    internal var topP: Float = GenerationSettings.DEFAULT_TOP_P
    internal var thinkingBudget: Int? = null

    fun GenerationConfig.temperature(temperature: Float) {
        this.temperature = temperature
    }

    fun GenerationConfig.topK(topK: Int) {
        this.topK = topK
    }

    fun GenerationConfig.topP(topP: Float) {
        this.topP = topP
    }

    fun GenerationConfig.thinkingBudget(thinkingBudget: Int) {
        this.thinkingBudget = thinkingBudget
    }

    override fun build(): GenerationSettings =
        GenerationSettings(
            temperature = temperature,
            topK = topK,
            topP = topP,
            thinkingBudget?.let(::ThinkingConfig),
        )
}
