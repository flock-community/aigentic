package community.flock.aigentic.koog

import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.prompt
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.message.ContextMessage
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.mapToTextMessages
import community.flock.aigentic.core.platform.Platform
import community.flock.aigentic.core.platform.getRuns

/**
 * Koog has no equivalent of the native DSL's example-run state (`state.addExampleMessage`), so this
 * pre-fetches runs tagged [tags] and splices them into a [Prompt] as extra user turns instead, using
 * the same marker wording as `fetchExampleRunMessages` in the core `AgentExecutor`.
 */
suspend inline fun <reified Output : Any> fetchExampleRunPrompt(
    platform: Platform,
    tags: List<RunTag>,
    systemPrompt: String,
    id: String = "koog-agent",
): Prompt {
    val runs = if (tags.isEmpty()) emptyList() else platform.getRuns<Output>(tags)

    return prompt(id) {
        system(systemPrompt)

        if (runs.isNotEmpty()) {
            user(
                "The messages below are example run messages. Each example run has a clearly marked start and end.\n" +
                    "The example messages continue until you encounter: <END_OF_ALL_EXAMPLES>",
            )

            runs.forEachIndexed { index, (_, run) ->
                user("The messages below are part of example $index. The example ends when you encounter: <END_EXAMPLE_$index>")
                run.messages
                    .filterIsInstance<ContextMessage>()
                    .firstOrNull()
                    ?.exampleText()
                    ?.let { user(it) }
                run.messages.mapToTextMessages().forEach { user(it.text) }
                user("<END_EXAMPLE_$index>.")
            }

            user(
                "<END_OF_ALL_EXAMPLES>\n" +
                    "All of the previous example messages are to be considered as the results of a desired run.\n" +
                    "Carefully analyze the relationship between the input (instructions, tool calls and arguments) and the output (responses).\n" +
                    "Use these relations in the current task and make sure to apply the instructions below to come to the same relationships.\n" +
                    "All messages following are the input for the current task.",
            )
        }
    }
}

@PublishedApi
internal fun ContextMessage.exampleText(): String? =
    when (this) {
        is Message.Text -> text
        is Message.ExampleToolMessage -> text
        else -> null
    }
