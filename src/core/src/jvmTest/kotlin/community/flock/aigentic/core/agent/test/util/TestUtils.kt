package community.flock.aigentic.core.agent.test.util

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.model.Usage
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

fun List<ToolCall>.toModelResponse() = map { it.toModelResponse() }

fun ToolCall.toModelResponse() =
    ModelResponse(
        Message.ToolCalls(listOf(this)),
        Usage(inputTokenCount = 100, outputTokenCount = 100, thinkingOutputTokenCount = 0),
    )

fun JsonObject.encode(): String = Json.encodeToString(this)

fun createMockModelWithException(exception: Exception): Model =
    mockk<Model>().also { mock ->
        coEvery { mock.sendRequest(any(), any(), any()) } throws exception
    }
