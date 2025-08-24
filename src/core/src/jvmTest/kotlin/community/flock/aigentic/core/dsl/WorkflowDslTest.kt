package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.workflow.Workflow2
import community.flock.aigentic.core.workflow.Workflow3
import community.flock.aigentic.core.workflow.Workflow4
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk

@AigenticParameter("String response")
data class StringResponse(val value: String)

@AigenticParameter("Int response")
data class IntResponse(val value: Int)

@AigenticParameter("Boolean response")
data class BooleanResponse(val value: Boolean)

@AigenticParameter("Final response")
data class FinalResponse(val result: String)

class WorkflowTest : DescribeSpec({

    describe("Workflow") {

        it("should create 3-agent workflow using thenProcess chaining") {
            val agent1 = mockk<Agent<String, IntResponse>>(relaxed = true)
            val agent2 = mockk<Agent<IntResponse, BooleanResponse>>(relaxed = true)
            val agent3 = mockk<Agent<BooleanResponse, FinalResponse>>(relaxed = true)

            val workflow = agent1 thenProcess agent2 thenProcess agent3

            workflow.shouldBeInstanceOf<Workflow3<String, IntResponse, BooleanResponse, FinalResponse>>()
            workflow.firstAgent shouldBe agent1
            workflow.restWorkflow.firstAgent shouldBe agent2
            workflow.restWorkflow.secondAgent shouldBe agent3
        }

        it("should create 4-agent workflow using thenProcess chaining") {
            val agent1 = mockk<Agent<String, IntResponse>>(relaxed = true)
            val agent2 = mockk<Agent<IntResponse, BooleanResponse>>(relaxed = true)
            val agent3 = mockk<Agent<BooleanResponse, StringResponse>>(relaxed = true)
            val agent4 = mockk<Agent<StringResponse, FinalResponse>>(relaxed = true)

            val workflow = agent1 thenProcess agent2 thenProcess agent3 thenProcess agent4

            workflow.shouldBeInstanceOf<Workflow4<String, IntResponse, BooleanResponse, StringResponse, FinalResponse>>()
            workflow.firstAgent shouldBe agent1
            workflow.restWorkflow.shouldBeInstanceOf<Workflow3<IntResponse, BooleanResponse, StringResponse, FinalResponse>>()
            workflow.restWorkflow.firstAgent shouldBe agent2
        }

        it("should validate Workflow3 structure correctly") {
            val agent1 = mockk<Agent<String, IntResponse>>(relaxed = true)
            val agent2 = mockk<Agent<IntResponse, BooleanResponse>>(relaxed = true)
            val agent3 = mockk<Agent<BooleanResponse, FinalResponse>>(relaxed = true)

            val workflow = agent1 thenProcess agent2 thenProcess agent3

            workflow.shouldBeInstanceOf<Workflow3<String, IntResponse, BooleanResponse, FinalResponse>>()
            workflow.firstAgent shouldBe agent1
            workflow.restWorkflow.shouldBeInstanceOf<Workflow2<IntResponse, BooleanResponse, FinalResponse>>()
            workflow.restWorkflow.firstAgent shouldBe agent2
            workflow.restWorkflow.secondAgent shouldBe agent3
        }

        it("should validate Workflow4 structure correctly") {
            val agent1 = mockk<Agent<String, IntResponse>>(relaxed = true)
            val agent2 = mockk<Agent<IntResponse, BooleanResponse>>(relaxed = true)
            val agent3 = mockk<Agent<BooleanResponse, StringResponse>>(relaxed = true)
            val agent4 = mockk<Agent<StringResponse, FinalResponse>>(relaxed = true)

            val workflow = agent1 thenProcess agent2 thenProcess agent3 thenProcess agent4

            workflow.shouldBeInstanceOf<Workflow4<String, IntResponse, BooleanResponse, StringResponse, FinalResponse>>()
            workflow.firstAgent shouldBe agent1
            workflow.restWorkflow.shouldBeInstanceOf<Workflow3<IntResponse, BooleanResponse, StringResponse, FinalResponse>>()
        }
    }
})
