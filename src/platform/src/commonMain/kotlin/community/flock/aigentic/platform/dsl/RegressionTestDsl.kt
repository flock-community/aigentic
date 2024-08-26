package community.flock.aigentic.platform.dsl

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.dsl.Config
import community.flock.aigentic.core.dsl.builderPropertyMissingErrorMessage
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.platform.testing.RegressionTest
import community.flock.aigentic.platform.testing.ToolCallOverride

fun regressionTest(regressionTestConfig: RegressionTestConfig.() -> Unit): RegressionTest = RegressionTestConfig().apply(regressionTestConfig).build()

class RegressionTestConfig : Config<RegressionTest> {
    internal var numberOfIterations: Int = 1
    internal val tags: MutableSet<String> = mutableSetOf()
    internal var agent: Agent? = null
    internal val toolCallOverrides: MutableList<ToolCallOverride> = mutableListOf()
    internal var contextMessageInterceptor: (List<Message>) -> List<Message> = { it }

    fun numberOfIterations(numberOfIterations: Int) {
        this.numberOfIterations = numberOfIterations
    }

    fun RegressionTestConfig.addTag(tag: String) {
        tags.add(tag)
    }

    fun RegressionTestConfig.tags(tags: Set<String>) {
        this.tags.addAll(tags)
    }

    fun RegressionTestConfig.agent(agent: Agent) {
        this.agent = agent
    }

    fun RegressionTestConfig.addExpectationOverride(
        toolCallId: String,
        arguments: String,
    ) {
        toolCallOverrides.add(ToolCallOverride(ToolCallId(toolCallId), arguments))
    }

    fun contextMessageInterceptor(contextMessageInterceptor: (List<Message>) -> List<Message>) {
        this.contextMessageInterceptor = contextMessageInterceptor
    }

    override fun build(): RegressionTest =
        RegressionTest(
            numberOfIterations = numberOfIterations,
            tags = tags.map { RunTag(it) },
            agent = checkNotNull(agent) { builderPropertyMissingErrorMessage("agent", "agent()") },
            toolCallOverrides = toolCallOverrides,
            contextMessageInterceptor = contextMessageInterceptor,
        )
}
