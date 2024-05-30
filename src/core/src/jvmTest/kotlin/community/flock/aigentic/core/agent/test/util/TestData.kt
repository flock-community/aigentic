package community.flock.aigentic.core.agent.test.util

import community.flock.aigentic.core.agent.tool.FINISH_OR_STUCK_TOOL_NAME
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.model.Model
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object TestData {
    val finishedSuccessfully =
        ToolCall(
            ToolCallId("1"),
            FINISH_OR_STUCK_TOOL_NAME,
            buildJsonObject {
                put("finishReason", "FinishedTask")
                put("description", "Finished the task")
            }.encode(),
        )

    val modelFinishDirectly =
        mockk<Model>().apply {
            coEvery { sendRequest(any(), any()) } returnsMany
                listOf(
                    finishedSuccessfully,
                ).toModelResponse()
        }
}
