package community.flock.aigentic.core.agent.message

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.Sender

@PublishedApi
internal val correctionMessage =
    Message.Text(
        sender = Sender.Agent,
        category = MessageCategory.EXECUTION,
        text = "Please only reply with tool calls.",
    )
