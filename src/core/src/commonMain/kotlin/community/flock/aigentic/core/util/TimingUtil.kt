package community.flock.aigentic.core.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@PublishedApi
internal suspend fun <T> withStartFinishTiming(block: suspend () -> T): Triple<Instant, Instant, T> {
    val startedAt = Clock.System.now()
    val result = block()
    val finishedAt = Clock.System.now()
    return Triple(startedAt, finishedAt, result)
}
