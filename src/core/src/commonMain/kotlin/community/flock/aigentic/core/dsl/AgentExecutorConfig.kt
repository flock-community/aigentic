package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.AgentExecutor
import community.flock.aigentic.core.agent.ToolInterceptor

fun agentExecutor(agentExecutorBuilder: AgentExecutorConfig.() -> Unit): AgentExecutor {
    return AgentExecutorConfig().apply(agentExecutorBuilder).build()
}

@AgentDSL
class AgentExecutorConfig : Config<AgentExecutor> {

    private val agents: MutableList<Agent> = mutableListOf()
    private var interceptors: MutableList<ToolInterceptor> = mutableListOf()

    fun AgentExecutorConfig.addAgent(agent: Agent) {
        agents.add(agent)
    }

    fun AgentExecutorConfig.addToolInterceptor(interceptor: ToolInterceptor) {
        this.interceptors.add(interceptor)
    }

    override fun build(): AgentExecutor = AgentExecutor(this.interceptors).also {
        it.loadAgents(agents)
    }
}
