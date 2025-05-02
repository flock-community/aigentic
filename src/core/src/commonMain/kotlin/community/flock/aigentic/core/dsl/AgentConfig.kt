package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.Context
import community.flock.aigentic.core.agent.Instruction
import community.flock.aigentic.core.agent.Task
import community.flock.aigentic.core.agent.message.DefaultSystemPromptBuilder
import community.flock.aigentic.core.agent.message.SystemPromptBuilder
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.TypedTool
import community.flock.aigentic.core.tool.getParameter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

fun agent(agentConfig: AgentConfig.() -> Unit): Agent = AgentConfig().apply(agentConfig).build()

@AgentDSL
class AgentConfig : Config<Agent> {
    internal var model: Model? = null
    internal var platform: Platform? = null
    internal var task: TaskConfig? = null
    internal var contexts: List<Context> = emptyList()
    internal var systemPromptBuilder: SystemPromptBuilder = DefaultSystemPromptBuilder
    val tools = mutableListOf<Tool>()
    var responseParameter: Parameter? = null

    fun AgentConfig.platform(platform: Platform) {
        this.platform = platform
    }

    fun AgentConfig.addTool(tool: Tool) = tools.add(tool)

    inline fun <reified I : Any, reified O : Any> AgentConfig.addTool(tool: TypedTool<I, O>) {
        tools.add(
            object : Tool {
                override val name: ToolName = tool.name
                override val description: String? = tool.description
                override val parameters: List<Parameter>
                    get() {
                        val parameter = getParameter<I>() ?: error("No parameter found for type ${I::class.simpleName}. Make sure the class has @AigenticParameter annotation and Aigentic.initialize() has been called.")
                        return parameter.parameters
                    }
                override val handler: suspend (toolArguments: JsonObject) -> String = {
                    val obj = Json.decodeFromJsonElement<I>(it)
                    val res = tool.handler(obj)
                    Json.encodeToString<O>(res)
                }
            },
        )
    }

    inline fun <reified O : Any> AgentConfig.addToolUnit(tool: TypedTool<Unit, O>) {
        tools.add(
            object : Tool {
                override val name: ToolName = tool.name
                override val description: String? = tool.description
                override val parameters: List<Parameter>
                    get() {
                        return emptyList()
                    }
                override val handler: suspend (toolArguments: JsonObject) -> String = {
                    val res = tool.handler(Unit)
                    Json.encodeToString<O>(res)
                }
            },
        )
    }

    inline fun <reified I : Any, reified O : Any> AgentConfig.addTool(
        name: String,
        description: String? = null,
        noinline handler: suspend (I) -> O,
    ) {
        val tool =
            object : TypedTool<I, O> {
                override val name = ToolName(name)
                override val description = description
                override val parameters = getParameter<I>()?.let { listOf(it) } ?: emptyList()
                override val handler: suspend (toolArguments: I) -> O = handler
            }

        addTool(tool)
    }

    inline fun <reified O : Any> AgentConfig.addToolUnit(
        name: String,
        description: String? = null,
        noinline handler: suspend (Unit) -> O,
    ) {
        val tool =
            object : TypedTool<Unit, O> {
                override val name = ToolName(name)
                override val description = description
                override val parameters = emptyList<Parameter>()
                override val handler: suspend (toolArguments: Unit) -> O = handler
            }

        addToolUnit(tool)
    }

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

    inline fun <reified T : Any> AgentConfig.finishResponse() {
        this.responseParameter = getParameter<T>()
    }

    override fun build(): Agent {
        return Agent(
            platform = platform,
            systemPromptBuilder = systemPromptBuilder,
            model = checkNotNull(model, builderPropertyMissingErrorMessage("model", "model()")),
            task = checkNotNull(task?.build(), builderPropertyMissingErrorMessage("task", "task()")),
            tools =
                check(
                    tools.isNotEmpty() || responseParameter != null,
                    builderPropertyMissingErrorMessage("tools", "addTool()"),
                ).let { tools.associateBy { it.name } },
            contexts = contexts,
            responseParameter = responseParameter,
        )
    }
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
