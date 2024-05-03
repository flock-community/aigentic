package community.flock.aigentic.dsl

import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.model.Authentication
import community.flock.aigentic.model.OpenAIModel
import community.flock.aigentic.model.OpenAIModelIdentifier

fun AgentConfig.openAIModel(
    apiKey: String,
    identifier: OpenAIModelIdentifier,
) = OpenAIModel(Authentication.APIKey(apiKey), identifier)
    .also { model = it }
