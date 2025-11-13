package community.flock.aigentic.core.message

/**
 * Categorizes messages by their purpose in the agent execution lifecycle.
 *
 * Categories determine how messages are:
 * - Stored (retention policies)
 * - Sent to platforms (filtering, routing)
 * - Displayed to users (UI filtering)
 * - Used in examples (learning)
 */
enum class MessageCategory {
    /**
     * The system prompt that defines agent behavior.
     * - Typically one per run
     * - Sent in ConfigDto
     * - Not visible in execution flow
     */
    SYSTEM_PROMPT,

    /**
     * Context provided via agent.context { } configuration.
     * - Set at agent creation time
     * - Sent in ConfigDto.contextMessages
     * - Reused across multiple runs
     */
    CONFIG_CONTEXT,

    /**
     * Attachments provided at run start time.
     * - Set when calling agent.start(attachments = ...)
     * - Sent in RunDto.runAttachmentMessages
     * - Specific to each run
     */
    RUN_CONTEXT,

    /**
     * Messages from example runs used for few-shot learning.
     * - NOT sent to platform (only example run IDs sent)
     * - Used to guide model behavior
     * - Filtered out of final run results
     */
    EXAMPLE,

    /**
     * Messages generated during agent execution.
     * - Tool calls, tool results, model responses
     * - Sent in RunDto.executionMessages
     * - The core conversation flow
     */
    EXECUTION;

    /**
     * Whether this category should be sent to the platform.
     */
    val isSentToPlatform: Boolean
        get() = this != EXAMPLE

    /**
     * Whether this category is set at agent configuration time.
     */
    val isConfigTime: Boolean
        get() = this in setOf(SYSTEM_PROMPT, CONFIG_CONTEXT)

    /**
     * Whether this category is set at runtime.
     */
    val isRunTime: Boolean
        get() = this in setOf(RUN_CONTEXT, EXECUTION)
}
