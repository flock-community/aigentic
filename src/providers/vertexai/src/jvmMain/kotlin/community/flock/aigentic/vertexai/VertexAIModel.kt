package community.flock.aigentic.vertexai

import com.google.genai.Client
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.vertexai.request.createGenerateConfig
import community.flock.aigentic.vertexai.request.createRequestContents
import community.flock.aigentic.vertexai.response.toModelResponse
import kotlinx.coroutines.future.await

@Suppress("ktlint:standard:class-naming")
sealed class VertexAIModelIdentifier(
    override val stringValue: String,
) : ModelIdentifier {
    data object Gemini2_0Flash : VertexAIModelIdentifier("gemini-2.0-flash")
    data object Gemini2_5FlashPreview : VertexAIModelIdentifier("gemini-2.5-flash-preview-05-20")
    data object Gemini2_5ProPreview : VertexAIModelIdentifier("gemini-2.5-pro-preview-05-06")

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
) : Model {
    private val client: Client = defaultVertexAIClient(project, location)

    override suspend fun sendRequest(
        messages: List<Message>,
        tools: List<ToolDescription>,
    ): ModelResponse =
        client.async.models.generateContent(
            modelIdentifier.stringValue,
            createRequestContents(messages),
            createGenerateConfig(messages, tools, generationSettings),
        ).await().toModelResponse()

    companion object {
        fun defaultVertexAIClient(
            project: Project,
            location: Location,
        ): Client {
            return Client.Builder()
                .vertexAI(true)
                .project(project.value)
                .location(location.value)
                .build()
        }
    }
}
