package community.flock.aigentic.platform.dsl

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.dsl.Config
import community.flock.aigentic.core.dsl.builderPropertyMissingErrorMessage
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.platform.testing.RegressionTest
import community.flock.aigentic.platform.testing.ToolCallOverride

fun <I : Any, O : Any> regressionTest(regressionTestConfig: RegressionTestConfig<I, O>.() -> Unit): RegressionTest<I, O> =
    RegressionTestConfig<I, O>().apply(
        regressionTestConfig,
    ).build()

class RegressionTestConfig<I : Any, O : Any> : Config<RegressionTest<I, O>> {
    internal var numberOfIterations: Int = 1
    internal val tags: MutableSet<String> = mutableSetOf()
    internal var agent: Agent<I, O>? = null
    internal val toolCallOverrides: MutableList<ToolCallOverride> = mutableListOf()
    internal var contextMessageInterceptor: (List<Message>) -> List<Message> = { it }

    fun numberOfIterations(numberOfIterations: Int) {
        this.numberOfIterations = numberOfIterations
    }

    fun RegressionTestConfig<I, O>.tags(vararg tags: String) {
        this.tags.addAll(tags)
    }

    fun RegressionTestConfig<I, O>.agent(agent: Agent<I, O>) {
        this.agent = agent
    }

    fun RegressionTestConfig<I, O>.addExpectationOverride(
        toolCallId: String,
        arguments: String,
    ) {
        toolCallOverrides.add(ToolCallOverride(ToolCallId(toolCallId), arguments))
    }

    fun contextMessageInterceptor(contextMessageInterceptor: (List<Message>) -> List<Message>) {
        this.contextMessageInterceptor = contextMessageInterceptor
    }

    override fun build(): RegressionTest<I, O> =
        RegressionTest(
            numberOfIterations = numberOfIterations,
            tags = tags.map { RunTag(it) },
            agent = checkNotNull(agent) { builderPropertyMissingErrorMessage("agent", "agent()") },
            toolCallOverrides = toolCallOverrides,
            contextMessageInterceptor = contextMessageInterceptor,
        )
}
