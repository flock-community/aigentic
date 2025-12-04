package community.flock.aigentic.vertexai

import com.google.genai.Client
import com.google.genai.types.ClientOptions
import com.google.genai.types.HttpOptions
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.vertexai.request.createGenerateConfig
import community.flock.aigentic.vertexai.request.createRequestContents
import community.flock.aigentic.vertexai.response.toModelResponse
import kotlinx.coroutines.future.await

@Suppress("ktlint:standard:class-naming")
sealed class VertexAIModelIdentifier(
    override val stringValue: String,
) : ModelIdentifier {
    data object Gemini2_5Flash : VertexAIModelIdentifier("gemini-2.5-flash")
    data object Gemini2_5Pro : VertexAIModelIdentifier("gemini-2.5-pro")
    data object Gemini2_0Flash : VertexAIModelIdentifier("gemini-2.0-flash")
    data object Gemini2_0FlashLite : VertexAIModelIdentifier("gemini-2.0-flash-lite")

    data class Custom(val identifier: String) : VertexAIModelIdentifier(identifier)
}

@JvmInline
value class Project(val value: String)

@JvmInline
value class Location(val value: String)

class VertexAIModel(
    override val modelIdentifier: ModelIdentifier,
    override val generationSettings: GenerationSettings,
    project: Project,
    location: Location,
    requestTimeoutMillis: Long,
) : Model {
    private val client: Client = defaultVertexAIClient(project, location, requestTimeoutMillis)

    override suspend fun sendRequest(
        messages: List<Message>,
        tools: List<ToolDescription>,
        structuredOutputParameter: Parameter?,
    ): ModelResponse =
        client.async.models.generateContent(
            modelIdentifier.stringValue,
            createRequestContents(messages),
            createGenerateConfig(messages, tools, generationSettings, structuredOutputParameter),
        ).await().toModelResponse(structuredOutputParameter != null)

    companion object {
        fun defaultVertexAIClient(
            project: Project,
            location: Location,
            requestTimeoutMillis: Long,
        ): Client =
            Client.Builder()
                .vertexAI(true)
                .httpOptions(HttpOptions.builder().timeout(requestTimeoutMillis.toInt()).build())
                .project(project.value)
                .location(location.value)
                .build()
    }
}
