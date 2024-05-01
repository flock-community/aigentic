package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.AgentExecutor
import community.flock.aigentic.core.tool.DefaultToolPermissionHandler
import community.flock.aigentic.core.tool.ToolPermissionHandler

fun agentExecutor(agentExecutorBuilder: AgentExecutorConfig.() -> Unit): AgentExecutor {
    return AgentExecutorConfig().apply(agentExecutorBuilder).build()
}

@AgentDSL
class AgentExecutorConfig : Config<AgentExecutor> {

    private val agents: MutableList<Agent> = mutableListOf()
    private var permissionHandler: ToolPermissionHandler = DefaultToolPermissionHandler()

    fun AgentExecutorConfig.addAgent(agent: Agent) {
        agents.add(agent)
    }

    fun AgentExecutorConfig.toolPermissionHandler(toolPermissionHandler: ToolPermissionHandler) {
        this.permissionHandler = toolPermissionHandler
    }

    override fun build(): AgentExecutor = AgentExecutor().also {
        it.permissionHandler = this.permissionHandler
        it.loadAgents(agents)
    }
}