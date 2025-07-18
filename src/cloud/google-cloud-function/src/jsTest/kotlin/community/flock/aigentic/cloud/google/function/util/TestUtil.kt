package community.flock.aigentic.cloud.google.function.util

import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.Message.ToolCalls
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.model.Usage
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.core.tool.createTool
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

val helloWorldTool =
    createTool<String, String>("testTool") { name ->
        "Hello from Google Cloud Function. $name 👋"
    }

fun modelFinishDirectly(finishToolCall: ToolCall = finishedTaskToolCall) =
    object : Model {
        override val modelIdentifier =
            object : ModelIdentifier {
                override val stringValue: String = "TestModelIdentifier"
            }
        override val generationSettings = GenerationSettings.DEFAULT

        override suspend fun sendRequest(
            messages: List<Message>,
            tools: List<ToolDescription>,
        ): ModelResponse = ModelResponse(ToolCalls(listOf(finishToolCall)), Usage.EMPTY)
    }

fun modelException() =
    object : Model {
        override val modelIdentifier =
            object : ModelIdentifier {
                override val stringValue: String = "TestModelIdentifier"
            }
        override val generationSettings = GenerationSettings.DEFAULT

        override suspend fun sendRequest(
            messages: List<Message>,
            tools: List<ToolDescription>,
        ): ModelResponse = aigenticException("Model API exception")
    }

val finishedTaskToolCall =
    ToolCall(
        ToolCallId("1"),
        "finishedTask",
        buildJsonObject {
            put("description", "Finished the task")
        }.let { Json.encodeToString(it) },
    )

val finishedTaskWithResponseToolCall =
    ToolCall(
        ToolCallId("1"),
        "finishedTask",
        buildJsonObject {
            put("description", "Finished the task")
            put("response", buildJsonObject { put("message", "Agent response") })
        }.let { Json.encodeToString(it) },
    )

val stuckWithTaskToolCall =
    ToolCall(
        ToolCallId("1"),
        "stuckWithTask",
        buildJsonObject {
            put("description", "I couldn't finish the task")
        }.let { Json.encodeToString(it) },
    )
