package community.flock.aigentic.core.agent.test.util

import community.flock.aigentic.core.agent.tool.finishedTaskTool
import community.flock.aigentic.core.agent.tool.stuckWithTaskTool
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.model.Model
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object TestData {
    val finishedTaskToolCall =
        ToolCall(
            ToolCallId("1"),
            finishedTaskTool.name.value,
            buildJsonObject {
                put("description", "Finished the task")
            }.encode(),
        )

    val stuckWithTaskToolCall =
        ToolCall(
            ToolCallId("1"),
            stuckWithTaskTool.name.value,
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
