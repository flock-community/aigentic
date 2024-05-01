package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.AgentExecutor
import community.flock.aigentic.core.agent.Schedule
import community.flock.aigentic.core.agent.ScheduleType
import community.flock.aigentic.core.tool.DefaultToolPermissionHandler
import community.flock.aigentic.core.tool.ToolPermissionHandler

fun agentExecutor(agentExecutorBuilder: AgentExecutorConfig.() -> Unit): AgentExecutor {
    return AgentExecutorConfig().apply(agentExecutorBuilder).build()
}

@AgentDSL
class AgentExecutorConfig : Config<AgentExecutor> {

    private val schedules: MutableList<Schedule> = mutableListOf()
    private var permissionHandler: ToolPermissionHandler = DefaultToolPermissionHandler()

    fun AgentExecutorConfig.schedule(scheduleType: ScheduleType, scheduleConfig: ScheduleConfig.() -> Unit) {
        ScheduleConfig(scheduleType).apply(scheduleConfig).build().also {
            this.schedules.add(it)
        }
    }

    fun AgentExecutorConfig.toolPermissionHandler(toolPermissionHandler: ToolPermissionHandler) {
        this.permissionHandler = toolPermissionHandler
    }

    override fun build(): AgentExecutor =
        AgentExecutor(schedules, permissionHandler)
}

@AgentDSL
class ScheduleConfig(
    private val scheduleType: ScheduleType
) : Config<Schedule> {

    private val agents = mutableListOf<Agent>()

    fun addAgent(agent: Agent) {
        agents.add(agent)
    }


    override fun build(): Schedule =
        Schedule(agents, scheduleType)
}
