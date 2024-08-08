package community.flock.aigentic.core.agent.message

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import community.flock.aigentic.core.agent.tool.STUCK_WITH_TASK_TOOL_NAME
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
        |Please execute one of these tools and the given context to fulfil these tasks. Don't send any text messages only use tool calls
        """.trimMargin()

    val instructions = task.instructions.joinToString(separator = "\n\n") { it.text }

    val finishConditionDescription =
        """
        |You are finished when the task is executed successfully: ${task.description}
        |If you meet this condition, call the $FINISHED_TASK_TOOL_NAME tool to indicate that you are done and have finished the task.
        |When you don't know what to do call the $STUCK_WITH_TASK_TOOL_NAME tool to indicate that you are stuck and need help.
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
