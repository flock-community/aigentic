package community.flock.aigentic.gemini.client.config

import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.gemini.model.GeminiModelIdentifier

data class GeminiApiConfig(
    val apiKey: Authentication.APIKey,
    val baseUrl: String = "https://generativelanguage.googleapis.com/v1beta/models",
    val numberOfRetriesOnServerErrors: Int = 2
)


