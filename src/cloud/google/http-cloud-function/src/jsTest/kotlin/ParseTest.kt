import community.flock.aigentic.cloud.google.httpcloudfunction.declarations.GoogleRequest
import io.kotest.core.spec.style.DescribeSpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ParseTest : DescribeSpec({

    it("should pass") {

        val jsObject = js("{ name: 'John' }")

        val googleRequest =
            object : GoogleRequest {
                override val method: String
                    get() = "GET"
                override val headers: dynamic
                    get() = jsObject
                override val query: dynamic
                    get() = jsObject
                override val body: String
                    get() = "body"
            }

        val jsonString = JSON.stringify(googleRequest.query)
        val map = Json.parseToJsonElement(jsonString).jsonObject.mapValues { it.value.jsonPrimitive.content }

        println(map)
        println(map["name"] ?: "No name")
    }
})
