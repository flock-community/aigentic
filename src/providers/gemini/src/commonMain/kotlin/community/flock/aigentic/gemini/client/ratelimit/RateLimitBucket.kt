package community.flock.aigentic.gemini.client.ratelimit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface RateLimiter {
    suspend fun consume()
}

internal data class RateLimitBucket(val requestsPerMinute: Int) : RateLimiter {
    private var requestsLeft: Int = requestsPerMinute
    private val delayTime = (60_000 / requestsPerMinute).toLong()

    init {
        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch {
            replenish()
        }
    }

    suspend fun replenish() {
        while (true) {
            delay(delayTime)
            if (requestsLeft < requestsPerMinute) {
                requestsLeft += 1
            }
        }
    }

    override suspend fun consume() {
        while (requestsLeft == 0) {
            delay(500)
        }
        requestsLeft -= 1
    }

    fun numberOfRequestsLeft(): Int = requestsLeft
}
