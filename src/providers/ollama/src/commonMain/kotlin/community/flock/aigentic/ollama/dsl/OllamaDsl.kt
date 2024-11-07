package community.flock.aigentic.ollama.dsl

import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.Config
import community.flock.aigentic.core.dsl.GenerationConfig
import community.flock.aigentic.core.dsl.builderPropertyMissingErrorMessage
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.openai.model.OpenAIApiUrl
import community.flock.aigentic.openai.model.OpenAIModel

fun AgentConfig.ollamaModel(ollamaConfig: OllamaConfig.() -> Unit) =
    OllamaConfig().apply(ollamaConfig).build().also { model(it) }
        .also { model(it) }

class OllamaConfig : Config<OpenAIModel> {
    private var modelIdentifier: ModelIdentifier? = null
    private var apiUrl: String = "http://localhost:11434/v1/"
    private var generationConfig: GenerationConfig = GenerationConfig()

    fun OllamaConfig.modelIdentifier(identifier: ModelIdentifier) {
        this.modelIdentifier = identifier
    }

    fun OllamaConfig.apiUrl(apiUrl: String) {
        this.apiUrl = apiUrl
    }

    fun OllamaConfig.generationConfig(generationConfig: GenerationConfig.() -> Unit) {
        this.generationConfig = GenerationConfig().apply(generationConfig)
    }

    override fun build(): OpenAIModel =
        OpenAIModel(
            authentication = Authentication.APIKey(""),
            modelIdentifier =
                checkNotNull(
                    modelIdentifier,
                    builderPropertyMissingErrorMessage("modelIdentifier", "ollamaModel { modelIdentifier() }"),
                ),
            generationSettings = generationConfig.build(),
            apiUrl =
                OpenAIApiUrl(
                    apiUrl.also {
                        check(it.isNotEmpty()) {
                            builderPropertyMissingErrorMessage(
                                "apiUrl",
                                "ollamaModel { apiUrl() }",
                            )
                        }
                    },
                ),
        )
}
