package community.flock.aigentic.gemini.client

import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.gemini.client.model.Candidate
import community.flock.aigentic.gemini.client.model.Content
import community.flock.aigentic.gemini.client.model.ErrorDetails
import community.flock.aigentic.gemini.client.model.GenerateContentResponse
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

internal suspend fun ByteReadChannel.readGenerateContentChunks(): List<GenerateContentResponse> =
    buildList {
        while (true) {
            val line = readLine() ?: break
            if (line.startsWith(SSE_DATA_PREFIX)) add(line.removePrefix(SSE_DATA_PREFIX).toGenerateContentChunk())
        }
    }

private fun String.toGenerateContentChunk(): GenerateContentResponse {
    val chunk = geminiJson.parseToJsonElement(this).jsonObject
    chunk["error"]?.let {
        val errorDetails = geminiJson.decodeFromJsonElement<ErrorDetails>(it)
        aigenticException(
            "Received error from Gemini while streaming the response, " +
                "error code: ${errorDetails.status}, " +
                "message: ${errorDetails.message}",
        )
    }
    return geminiJson.decodeFromJsonElement(chunk)
}

internal fun List<GenerateContentResponse>.merge(): GenerateContentResponse {
    val promptFeedback = firstNotNullOfOrNull { it.promptFeedback }
    val candidates = mapNotNull { it.candidates?.firstOrNull() }
    val finishReason = candidates.mapNotNull { it.finishReason }.lastOrNull()

    if (promptFeedback?.blockReason == null && finishReason == null) {
        aigenticException("Gemini stream ended before the response was complete")
    }

    val contents = candidates.mapNotNull { it.content }
    val content = contents.firstOrNull()?.let { Content(it.role, contents.flatMap(Content::parts)) }

    return GenerateContentResponse(
        candidates = if (candidates.isEmpty()) null else listOf(Candidate(content, finishReason)),
        promptFeedback = promptFeedback,
        usageMetadata = mapNotNull { it.usageMetadata }.lastOrNull(),
    )
}

private const val SSE_DATA_PREFIX = "data:"
