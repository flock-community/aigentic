package community.flock.aigentic.tools.http

import community.flock.aigentic.core.tool.PrimitiveValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.property.checkAll

class EndpointOperationsTest : DescribeSpec({

    describe("PrimitiveValue") {

        it("String") {
            checkAll<String> { string ->
                PrimitiveValue.String.fromString(string)
            }
        }

        it("Number") {
            checkAll<Int> { number ->
                PrimitiveValue.Number.fromString(number.toString())
                PrimitiveValue.Integer.fromString(number.toString())
            }
        }

        it("Boolean") {
            checkAll<Boolean> { boolean ->
                PrimitiveValue.Boolean.fromString(boolean.toString())
            }
        }
    }
})
