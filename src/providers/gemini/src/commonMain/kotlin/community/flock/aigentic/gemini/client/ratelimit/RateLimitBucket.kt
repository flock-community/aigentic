package community.flock.aigentic.gemini.client.ratelimit

import kotlinx.coroutines.delay

data class RateLimitBucket(val requestsPerMinute: Int) {

    private var requestsLeft: Int = requestsPerMinute

    private val delayTime = (60 / requestsPerMinute).toLong()

    suspend fun replenish() {

        if(requestsLeft * delayTime <= 60) {
            // Do nothing
        } else {
            requestsLeft += 1
        }
        delay(delayTime)

    }
}
