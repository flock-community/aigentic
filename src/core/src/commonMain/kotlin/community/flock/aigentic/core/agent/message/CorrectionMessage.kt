package community.flock.aigentic.core.agent.message

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Sender

@PublishedApi
internal val correctionMessage =
    Message.Text(
        Sender.Agent,
        text = "Please only reply with tool calls.",
    )
