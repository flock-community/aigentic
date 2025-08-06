package community.flock.aigentic.gemini.dsl

import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.Config
import community.flock.aigentic.core.dsl.GenerationConfig
import community.flock.aigentic.core.dsl.builderPropertyMissingErrorMessage
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.core.model.LogLevel
import community.flock.aigentic.gemini.model.GeminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier

fun <I : Any, O : Any> AgentConfig<I, O>.geminiModel(geminiModelConfig: GeminiModelConfig.() -> Unit) =
    GeminiModelConfig().apply(geminiModelConfig).build().also { model(it) }

class GeminiModelConfig : Config<GeminiModel> {
    private var apiKey: String? = null
    private var modelIdentifier: GeminiModelIdentifier? = null
    private var generationConfig: GenerationConfig = GenerationConfig()
    private var logLevel: LogLevel = LogLevel.NONE
    private var requestTimeoutMillis: Long = 60_000
    private var socketTimeoutMillis: Long = 60_000

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

    fun GeminiModelConfig.requestTimeout(timeoutMillis: Long) {
        this.requestTimeoutMillis = timeoutMillis
    }

    fun GeminiModelConfig.socketTimeout(timeoutMillis: Long) {
        this.socketTimeoutMillis = timeoutMillis
    }

    override fun build(): GeminiModel {
        val authentication =
            Authentication.APIKey(
                checkNotNull(
                    apiKey,
                    builderPropertyMissingErrorMessage("apiKey", "geminiModel { apiKey() }"),
                ),
            )

        return GeminiModel(
            authentication = authentication,
            modelIdentifier =
                checkNotNull(
                    modelIdentifier,
                    builderPropertyMissingErrorMessage("modelIdentifier", "geminiModel { modelIdentifier() }"),
                ),
            generationSettings = generationConfig.build(),
            logLevel = logLevel,
            geminiClient =
                GeminiModel.defaultGeminiClient(
                    apiKeyAuthentication = authentication,
                    logLevel = logLevel,
                    requestTimeoutMillis = requestTimeoutMillis,
                    socketTimeoutMillis = socketTimeoutMillis,
                ),
        )
    }
}
