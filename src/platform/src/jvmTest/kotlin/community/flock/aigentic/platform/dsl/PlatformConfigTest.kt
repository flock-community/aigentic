package community.flock.aigentic.platform.dsl

import community.flock.aigentic.core.dsl.agent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class PlatformConfigTest : DescribeSpec({

    describe("PlatformConfig") {

        it("should build platform with basic authentication") {

            agent {
                platform {
                    name("some-name")
                    secret("some-secret")
                }
                model(mockk(relaxed = true))
                task("Task description") {}
                addTool(mockk(relaxed = true))
            }.run {
                val basicAuth = platform!!.authentication
                basicAuth.username shouldBe "some-name"
                basicAuth.password shouldBe "some-secret"
            }
        }
    }

    withData(
        nameFn = { "Should fail with missing property: $it" },
        MissingPropertyCase(
            null,
            "some-password",
            "Cannot build Platform, property 'name' is missing, please use 'platform { name() }' to provide it",
        ),
        MissingPropertyCase(
            "some-name",
            null,
            "Cannot build Platform, property 'secret' is missing, please use 'platform { secret() }' to provide it",
        ),
        MissingPropertyCase(
            null,
            null,
            "Cannot build Platform, property 'name' is missing, please use 'platform { name() }' to provide it",
        ),
    ) {
        shouldThrow<IllegalStateException> {
            agent {
                platform {
                    it.name?.let { name -> name(name) }
                    it.secret?.let { secret -> secret(secret) }
                }
                model(mockk(relaxed = true))
                task("Task description") {}
                addTool(mockk(relaxed = true))
            }
        }.run {
            message shouldBe it.expectedErrorMessage
        }
    }
})

data class MissingPropertyCase(val name: String?, val secret: String?, val expectedErrorMessage: String)
