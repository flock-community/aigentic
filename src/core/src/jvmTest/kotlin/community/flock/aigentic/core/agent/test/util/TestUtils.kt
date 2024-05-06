package community.flock.aigentic.core.agent.test.util

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.model.ModelResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

fun List<ToolCall>.toModelResponse() = map { it.toModelResponse() }
fun ToolCall.toModelResponse() = ModelResponse(Message.ToolCalls(listOf(this)))


fun JsonObject.encode(): String = Json.encodeToString(this)
