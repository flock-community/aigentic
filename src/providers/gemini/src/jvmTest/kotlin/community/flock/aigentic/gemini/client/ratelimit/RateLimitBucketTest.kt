package community.flock.aigentic.gemini.client.ratelimit

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis

class RateLimitBucketTest : DescribeSpec({

    describe("RateLimitBucket") {

        it("should initialize with the correct number of requests left") {
            val time = measureTimeMillis {
                val bucket = RateLimitBucket(5)
                bucket.numberOfRequestsLeft() shouldBe 5
            }
            time shouldBeLessThan 100
        }

        it("should consume a request") {
            val time = measureTimeMillis {
                val bucket = RateLimitBucket(5)
                bucket.consume()
                bucket.numberOfRequestsLeft() shouldBe 4
            }
            time shouldBeLessThan 100
        }

        it("should delay after bucket is exhausted") {

            val rpm = 5
            val bucket = RateLimitBucket(rpm)

            val time1 = measureTimeMillis {
                repeat(rpm) { bucket.consume() }
                bucket.consume()
            }

            bucket.numberOfRequestsLeft() shouldBe 0

            // 5 rpm = 60_000 / 5 = 12_000
            time1 shouldBeGreaterThan 12_000

            val time2 = measureTimeMillis {
                bucket.consume()
            }

            bucket.numberOfRequestsLeft() shouldBe 0
            time2 shouldBeGreaterThan 12_000
        }

        it("should not contain more tokens than the rate limit").config(coroutineTestScope = true) {
            val rpm = 500
            val bucket = RateLimitBucket(rpm)
            delay(500)
            bucket.numberOfRequestsLeft() shouldBe rpm
        }
    }
})
