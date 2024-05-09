package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.tool.FinishedOrStuck
import community.flock.aigentic.core.message.Message
import kotlinx.datetime.Instant

data class Run(
    val startedAt: Instant,
    val finishedAt: Instant,
    val messages: List<Message>,
    val result: FinishedOrStuck
)
