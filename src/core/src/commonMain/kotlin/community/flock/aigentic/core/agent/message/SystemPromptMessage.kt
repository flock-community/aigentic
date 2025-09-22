package community.flock.aigentic.core.agent.message

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import community.flock.aigentic.core.agent.tool.STUCK_WITH_TASK_TOOL_NAME
import community.flock.aigentic.core.message.Message

interface SystemPromptBuilder {
    fun <I : Any, O : Any> buildSystemPrompt(agent: Agent<I, O>): Message.SystemPrompt
}

data object AgenticSystemPromptBuilder : SystemPromptBuilder {
    override fun <I : Any, O : Any> buildSystemPrompt(agent: Agent<I, O>): Message.SystemPrompt = agent.createAgenticSystemPrompt()
}

data object StructuredOutputPromptBuilder : SystemPromptBuilder {
    override fun <I : Any, O : Any> buildSystemPrompt(agent: Agent<I, O>): Message.SystemPrompt = agent.createStructuredOutputSystemPrompt()
}

private fun <I : Any, O : Any> Agent<I, O>.createAgenticSystemPrompt(): Message.SystemPrompt {
    val baseInstruction =
        """
        |You are an AI agent designed to accomplish a specified task.
        |Your primary mode of operation is through tool calls. Do not generate text responses.
        |Always use the provided tools to complete your assigned task.
        """.trimMargin()

    val contextInstruction =
        if (contexts.isNotEmpty()) {
            """
        |Essential context for this task is provided in the initial messages.
        |Carefully analyze and utilize this context to inform your actions and decision-making.
            """.trimMargin()
        } else {
            ""
        }

    val instructions = task.instructions.joinToString(separator = "\n") { it.text }

    val finishConditionDescription =
        """
        |Task Completion Protocol:
        |1. Execute the task using the provided tools until it is successfully completed.
        |2. Once the task is finished, call the $FINISHED_TASK_TOOL_NAME tool to signal completion.
        |3. If you encounter difficulties or uncertainties, immediately call the $STUCK_WITH_TASK_TOOL_NAME tool to request assistance.
        |4. Do not attempt to continue or guess if you're unsure; always use the appropriate tool to signal your status.
        """.trimMargin()

    return Message.SystemPrompt(
        """
        |$baseInstruction
        |$contextInstruction

        |Assigned Task:
        |${task.description}

        |Specific Instructions:
        |$instructions

        |$finishConditionDescription

        |Remember: Your responses should consist solely of tool calls. Do not generate any other form of text output.
        """.trimMargin(),
    )
}

private fun <I : Any, O : Any> Agent<I, O>.createStructuredOutputSystemPrompt(): Message.SystemPrompt {
    val baseInstruction =
        """
        |You are an AI agent designed to accomplish a specified task.
        |Your response must be structured data that matches the expected output format.
        """.trimMargin()

    val contextInstruction =
        if (contexts.isNotEmpty()) {
            """
        |Essential context for this task is provided in the initial messages.
        |Carefully analyze and utilize this context to inform your response.
            """.trimMargin()
        } else {
            ""
        }

    val instructions = task.instructions.joinToString(separator = "\n") { it.text }

    val outputInstruction =
        """
        |Output Format:
        |Your response must be structured data that can be parsed into the expected output type.
        |Ensure your response contains all required fields and follows the specified format exactly.
        """.trimMargin()

    return Message.SystemPrompt(
        """
        |$baseInstruction
        |$contextInstruction

        |Assigned Task:
        |${task.description}

        |Specific Instructions:
        |$instructions

        |$outputInstruction
        """.trimMargin(),
    )
}
