package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.Aigentic
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.getParameter
import community.flock.aigentic.generated.parameter.initialize
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class AigenticInitializeTest : DescribeSpec({

    describe("Aigentic.initialize Tests") {
        it("should initialize ParameterRegistry when Aigentic.initialize is called") {
            Aigentic.initialize()

            val personParam = getParameter<Person>()
            personParam shouldNotBe null
            personParam.shouldBeInstanceOf<Parameter.Complex.Object>()
        }
    }
})
