package community.flock.aigentic.openai.dsl

import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.openai.model.OpenAIModel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier

fun AgentConfig.openAIModel(
    apiKey: String,
    identifier: OpenAIModelIdentifier,
) = OpenAIModel(Authentication.APIKey(apiKey), identifier)
    .also { model(it) }
