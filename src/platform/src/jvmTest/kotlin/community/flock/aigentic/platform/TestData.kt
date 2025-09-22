package community.flock.aigentic.platform

import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.model.Usage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun List<ToolCall>.toModelResponse() = map { it.toModelResponse() }

fun ToolCall.toModelResponse() =
    ModelResponse(
        Message.ToolCalls(listOf(this)),
        Usage(inputTokenCount = 100, outputTokenCount = 100, thinkingOutputTokenCount = 0),
    )

object TestData {
    val finishedTaskToolCall =
        ToolCall(
            ToolCallId("1"),
            FINISHED_TASK_TOOL_NAME,
            buildJsonObject {
                put("description", "Finished the task")
            }.run { Json.encodeToString(this) },
        )
}
