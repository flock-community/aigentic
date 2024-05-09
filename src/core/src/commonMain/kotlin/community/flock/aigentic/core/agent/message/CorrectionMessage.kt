package community.flock.aigentic.core.agent.message

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Sender

internal val correctionMessage = Message.Text(Sender.Aigentic, "Please only reply with tool calls.")
