package community.flock.aigentic.koog.model

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ToolDescription

class KoogModelIdentifier(
    provider: String,
    id: String,
) : ModelIdentifier {
    override val stringValue: String = "$provider/$id"
}

class KoogModel(
    override val modelIdentifier: ModelIdentifier,
    override val generationSettings: GenerationSettings = GenerationSettings.DEFAULT,
) : Model {
    override suspend fun sendRequest(
        messages: List<Message>,
        tools: List<ToolDescription>,
        structuredOutputParameter: Parameter?,
    ): ModelResponse = error("KoogModel is a run-reporting placeholder and should never be invoked")
}
