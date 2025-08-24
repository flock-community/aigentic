package community.flock.aigentic.core.agent.test.util

import community.flock.aigentic.core.agent.tool.FINISHED_TASK_TOOL_NAME
import community.flock.aigentic.core.agent.tool.STUCK_WITH_TASK_TOOL_NAME
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.model.Model
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

object TestData {
    inline fun <reified T : Any> finishedTaskWithResponseToolCall(response: T) =
        ToolCall(
            ToolCallId("1"),
            FINISHED_TASK_TOOL_NAME,
            buildJsonObject {
                put("description", "Finished the task")
                put(response::class.simpleName!!, Json.encodeToJsonElement(response))
            }.encode(),
        )

    val finishedTaskToolCall =
        ToolCall(
            ToolCallId("1"),
            FINISHED_TASK_TOOL_NAME,
            buildJsonObject {
                put("description", "Finished the task")
            }.encode(),
        )

    val stuckWithTaskToolCall =
        ToolCall(
            ToolCallId("1"),
            STUCK_WITH_TASK_TOOL_NAME,
            buildJsonObject {
                put("description", "I don't know what to do")
            }.encode(),
        )

    val modelFinishTaskDirectly =
        mockk<Model>().apply {
            coEvery { sendRequest(any(), any()) } returnsMany
                listOf(
                    finishedTaskToolCall,
                ).toModelResponse()
        }

    val modelStuckDirectly =
        mockk<Model>().apply {
            coEvery { sendRequest(any(), any()) } returnsMany
                listOf(
                    stuckWithTaskToolCall,
                ).toModelResponse()
        }
}
