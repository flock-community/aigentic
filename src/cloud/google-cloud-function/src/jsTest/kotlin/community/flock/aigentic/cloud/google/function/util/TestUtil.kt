package community.flock.aigentic.cloud.google.function.util

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Message.ToolCalls
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.core.tool.ToolName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

val testTool =
    object : Tool {
        override val name: ToolName = ToolName("TestTool")
        override val description: String? = null
        override val parameters: List<Parameter> = emptyList()
        override val handler: suspend (map: JsonObject) -> String = {
            "Hello from Google Cloud Function 👋"
        }
    }

fun modelFinishDirectly(finishReason: ToolCall = finishedTask) =
    object : Model {
        override val authentication = Authentication.APIKey("some-secret")
        override val modelIdentifier = object : ModelIdentifier {}

        override suspend fun sendRequest(
            messages: List<Message>,
            tools: List<ToolDescription>,
        ): ModelResponse = ModelResponse(ToolCalls(listOf(finishReason)))
    }

val finishedTask =
    ToolCall(
        ToolCallId("1"),
        "finishedOrStuck",
        buildJsonObject {
            put("finishReason", "FinishedTask")
            put("description", "Finished the task")
        }.let { Json.encodeToString(it) },
    )

val finishedTaskWithResponse =
    ToolCall(
        ToolCallId("1"),
        "finishedOrStuck",
        buildJsonObject {
            put("finishReason", "FinishedTask")
            put("description", "Finished the task")
            put("response", buildJsonObject { put("message", "Agent response") })
        }.let { Json.encodeToString(it) },
    )

val imStuck =
    ToolCall(
        ToolCallId("1"),
        "finishedOrStuck",
        buildJsonObject {
            put("finishReason", "ImStuck")
            put("description", "I couldn't finish the task")
        }.let { Json.encodeToString(it) },
    )
