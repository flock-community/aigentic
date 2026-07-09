package community.flock.aigentic.koog

import ai.koog.agents.core.feature.config.FeatureConfig
import community.flock.aigentic.core.agent.RunId
import community.flock.aigentic.core.agent.RunTag
import community.flock.aigentic.core.agent.Task
import community.flock.aigentic.core.platform.Platform
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

/**
 * Configuration for the [Aigentic] Koog feature, installed via `install(Aigentic) { ... }`.
 *
 * `task` has no default - it's the one field with real product meaning (what the platform groups
 * evaluations/annotations under), so a placeholder default would quietly degrade data quality
 * rather than remove real boilerplate. Everything else defaults for the common case: a Koog agent
 * whose output is plain `String` (Koog's own default), reporting with no tags and no example-run
 * seeding, against a platform client built from `AIGENTIC_PLATFORM_NAME`/`_SECRET`/`_URL`.
 *
 * Agents with structured (non-`String`) output must call [outputType], e.g. `outputType<WeatherAnswer>()`.
 */
class AigenticConfig : FeatureConfig() {
    private var explicitPlatform: Platform? = null

    // Only falls back to defaultAigenticPlatform() (which requires the AIGENTIC_PLATFORM_* env
    // vars) when read without ever having been set explicitly - so `install(Aigentic) { platform =
    // myPlatform; ... }` never constructs-and-discards a throwaway default, and doesn't require
    // those env vars to be present at all.
    var platform: Platform
        get() = explicitPlatform ?: defaultAigenticPlatform().also { explicitPlatform = it }
        set(value) {
            explicitPlatform = value
        }

    lateinit var task: Task
    var tags: List<RunTag> = emptyList()
    var exampleRunIds: List<RunId> = emptyList()

    @Suppress("UNCHECKED_CAST")
    var outputSerializer: KSerializer<Any> = String.serializer() as KSerializer<Any>
    var onRunReported: (RunId) -> Unit = {}
}

/**
 * Sets [AigenticConfig.outputSerializer] for a structured (non-`String`) Koog agent output type,
 * e.g. `outputType<WeatherAnswer>()` inside `install(Aigentic) { ... }`.
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified Output : Any> AigenticConfig.outputType() {
    outputSerializer = kotlinx.serialization.serializer<Output>() as KSerializer<Any>
}
