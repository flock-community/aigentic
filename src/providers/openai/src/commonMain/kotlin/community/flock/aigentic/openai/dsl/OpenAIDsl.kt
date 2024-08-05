package community.flock.aigentic.openai.dsl

import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.AgentDSL
import community.flock.aigentic.core.dsl.Config
import community.flock.aigentic.core.dsl.builderPropertyMissingErrorMessage
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.openai.model.OpenAIApiUrl
import community.flock.aigentic.openai.model.OpenAIModel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier

fun AgentConfig.openAIModel(openAIModelConfig: OpenAIModelConfig.() -> Unit) =
    OpenAIModelConfig().apply(openAIModelConfig).build().also { model(it) }
        .also { model(it) }

@AgentDSL
class OpenAIModelConfig : Config<OpenAIModel> {
    private var apiKey: String? = null
    private var modelIdentifier: OpenAIModelIdentifier? = null

    fun OpenAIModelConfig.apiKey(apiKey: String) {
        this.apiKey = apiKey
    }

    fun OpenAIModelConfig.modelIdentifier(identifier: OpenAIModelIdentifier) {
        this.modelIdentifier = identifier
    }

    override fun build(): OpenAIModel =
        OpenAIModel(
            authentication =
                Authentication.APIKey(
                    checkNotNull(
                        apiKey,
                        builderPropertyMissingErrorMessage("apiKey", "openAIModel { apiKey() }"),
                    ),
                ),
            modelIdentifier =
                checkNotNull(
                    modelIdentifier,
                    builderPropertyMissingErrorMessage("modelIdentifier", "openAIModel { modelIdentifier() }"),
                ),
            apiUrl = OpenAIApiUrl("https://api.openai.com/v1/"),
        )
}
