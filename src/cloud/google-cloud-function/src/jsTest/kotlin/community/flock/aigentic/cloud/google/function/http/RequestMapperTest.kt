package community.flock.aigentic.cloud.google.function.http

import community.flock.aigentic.cloud.google.function.declarations.GoogleRequest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class RequestMapperTest : DescribeSpec({

    val googleRequest =
        object : GoogleRequest {
            override val method: String = "GET"
            override val headers: dynamic = js("{ authorization: 'Bearer some-secret', 'content-type': 'application/json' }")
            override val query: dynamic = js("{ name: 'John'}")
            override val body: dynamic = js("{ message: 'Hello World' }")
        }

    it("should map request") {

        with(googleRequest.map()) {
            method shouldBe "GET"
            headers["authorization"] shouldBe "Bearer some-secret"
            headers["content-type"] shouldBe "application/json"
            query["name"] shouldBe "John"
            body.jsonObject["message"]?.jsonPrimitive?.content shouldBe "Hello World"
        }
    }
})
