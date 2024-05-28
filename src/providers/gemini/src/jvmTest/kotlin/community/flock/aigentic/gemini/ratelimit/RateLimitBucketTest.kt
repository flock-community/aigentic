package community.flock.aigentic.gemini.ratelimit

import community.flock.aigentic.gemini.client.ratelimit.RateLimitBucket
import io.kotest.common.runBlocking
import io.kotest.core.spec.style.DescribeSpec
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RateLimitBucketTest: DescribeSpec({

    it("should replenish") {
        runBlocking {

            val bucket = RateLimitBucket(5)

            async {
                bucket.replenish()
            }





        }

    }
})
