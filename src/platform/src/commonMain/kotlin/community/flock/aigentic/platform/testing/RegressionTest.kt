package community.flock.aigentic.platform.testing

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCallId
import kotlin.jvm.JvmInline

class RegressionTest(
    val numberOfIterations: Int,
    val tags: List<RunTag>,
    val agent: Agent,
    val toolCallOverrides: List<ToolCallOverride>,
    val contextMessageInterceptor: (List<Message>) -> List<Message>,
)

@JvmInline
value class RunTag(val value: String)

data class ToolCallOverride(
    val toolCallId: ToolCallId,
    val arguments: String,
)
