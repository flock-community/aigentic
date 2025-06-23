package community.flock.aigentic.vertexai.dsl

import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.Config
import community.flock.aigentic.core.dsl.GenerationConfig
import community.flock.aigentic.core.dsl.builderPropertyMissingErrorMessage
import community.flock.aigentic.vertexai.Location
import community.flock.aigentic.vertexai.Project
import community.flock.aigentic.vertexai.VertexAIModel
import community.flock.aigentic.vertexai.VertexAIModelIdentifier

fun <I : Any, O : Any> AgentConfig<I, O>.vertexAIModel(vertexAIModelConfig: VertexAIModelConfig.() -> Unit) =
    VertexAIModelConfig().apply(
        vertexAIModelConfig,
    ).build().also {
        model(it)
    }

class VertexAIModelConfig : Config<VertexAIModel> {
    private var project: String? = null
    private var location: String? = null
    private var modelIdentifier: VertexAIModelIdentifier? = null
    private var generationConfig: GenerationConfig = GenerationConfig()

    fun VertexAIModelConfig.project(project: String) {
        this.project = project
    }

    fun VertexAIModelConfig.location(location: String) {
        this.location = location
    }

    fun VertexAIModelConfig.modelIdentifier(identifier: VertexAIModelIdentifier) {
        this.modelIdentifier = identifier
    }

    fun VertexAIModelConfig.generationConfig(generationConfig: GenerationConfig.() -> Unit) {
        this.generationConfig = GenerationConfig().apply(generationConfig)
    }

    override fun build(): VertexAIModel =
        VertexAIModel(
            project =
                checkNotNull(
                    project,
                    builderPropertyMissingErrorMessage("project", "vertexAIModel { project() }"),
                ).let(::Project),
            location =
                checkNotNull(
                    location,
                    builderPropertyMissingErrorMessage("location", "vertexAIModel { location() }"),
                ).let(::Location),
            modelIdentifier =
                checkNotNull(
                    modelIdentifier,
                    builderPropertyMissingErrorMessage("modelIdentifier", "vertexAIModel { modelIdentifier() }"),
                ),
            generationSettings = generationConfig.build(),
        )
}
