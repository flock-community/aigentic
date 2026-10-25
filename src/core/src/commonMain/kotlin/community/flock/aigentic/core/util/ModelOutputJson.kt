package community.flock.aigentic.core.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
@PublishedApi
internal val modelOutputJson = Json { explicitNulls = false }
