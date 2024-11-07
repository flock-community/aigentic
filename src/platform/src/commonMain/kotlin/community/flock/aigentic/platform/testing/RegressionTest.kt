package community.flock.aigentic.platform.testing

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCallId

class RegressionTest(
    val numberOfIterations: Int,
    val tags: List<RunTag>,
    val agent: Agent,
    val toolCallOverrides: List<ToolCallOverride>,
    val contextMessageInterceptor: (List<Message>) -> List<Message>,
)

data class ToolCallOverride(
    val toolCallId: ToolCallId,
    val arguments: String,
)
