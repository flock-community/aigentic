package community.flock.aigentic.core.agent.message

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.tool.finishOrStuckTool
import community.flock.aigentic.core.message.Message

interface SystemPromptBuilder {
    fun buildSystemPrompt(agent: Agent): Message.SystemPrompt
}

data object DefaultSystemPromptBuilder : SystemPromptBuilder {
    override fun buildSystemPrompt(agent: Agent): Message.SystemPrompt = agent.createSystemPrompt()
}

private fun Agent.createSystemPrompt(): Message.SystemPrompt {
    val baseInstruction =
        """
        |You are an agent which helps the user to accomplish different tasks. These tasks are outlined by the user below.
        |The user also gives you information which gives you context, these are the first messages.
        |Please execute one of these tools and the given context to fulfil these tasks. Don't send any text messages only use tools
        """.trimMargin()

    val instructions = task.instructions.joinToString(separator = "\n\n")

    val finishConditionDescription =
        """
        |You are finished when the task is executed successfully: ${task.description}
        |If you meet this condition, call the ${finishOrStuckTool.name.value} tool to indicate that you are done and have finished all tasks.
        |When you don't know what to do also call the ${finishOrStuckTool.name.value} tool to indicate that you are stuck and need help.
        """.trimMargin()

    return Message.SystemPrompt(
        """
        |$baseInstruction

        |Instructions:
        |$instructions

        |$finishConditionDescription
        """.trimMargin(),
    )
}