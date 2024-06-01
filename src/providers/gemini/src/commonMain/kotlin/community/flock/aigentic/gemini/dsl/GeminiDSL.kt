package community.flock.aigentic.gemini.dsl

import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.gemini.model.GeminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier

fun AgentConfig.geminiModel(
    apiKey: String,
    identifier: GeminiModelIdentifier,
) = GeminiModel(Authentication.APIKey(apiKey), identifier)
    .also { model(it) }
