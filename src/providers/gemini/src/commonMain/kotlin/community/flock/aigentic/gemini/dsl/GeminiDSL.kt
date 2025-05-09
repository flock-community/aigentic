package community.flock.aigentic.gemini.dsl

import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.Config
import community.flock.aigentic.core.dsl.GenerationConfig
import community.flock.aigentic.core.dsl.builderPropertyMissingErrorMessage
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.LogLevel
import community.flock.aigentic.gemini.model.GeminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier

fun AgentConfig.geminiModel(geminiModelConfig: GeminiModelConfig.() -> Unit) =
    GeminiModelConfig().apply(geminiModelConfig).build().also { model(it) }
        .also { model(it) }

class GeminiModelConfig : Config<GeminiModel> {
    private var apiKey: String? = null
    private var modelIdentifier: GeminiModelIdentifier? = null
    private var generationConfig: GenerationConfig = GenerationConfig()
    private var logLevel: LogLevel = LogLevel.NONE

    fun GeminiModelConfig.apiKey(apiKey: String) {
        this.apiKey = apiKey
    }

    fun GeminiModelConfig.modelIdentifier(identifier: GeminiModelIdentifier) {
        this.modelIdentifier = identifier
    }

    fun GeminiModelConfig.generationConfig(generationConfig: GenerationConfig.() -> Unit) {
        this.generationConfig = GenerationConfig().apply(generationConfig)
    }

    fun GeminiModelConfig.logLevel(level: LogLevel) {
        this.logLevel = level
    }

    override fun build(): GeminiModel =
        GeminiModel(
            authentication =
                Authentication.APIKey(
                    checkNotNull(
                        apiKey,
                        builderPropertyMissingErrorMessage("apiKey", "geminiModel { apiKey() }"),
                    ),
                ),
            modelIdentifier =
                checkNotNull(
                    modelIdentifier,
                    builderPropertyMissingErrorMessage("modelIdentifier", "geminiModel { modelIdentifier() }"),
                ),
            generationSettings = generationConfig.build(),
            logLevel = logLevel,
        )
}
