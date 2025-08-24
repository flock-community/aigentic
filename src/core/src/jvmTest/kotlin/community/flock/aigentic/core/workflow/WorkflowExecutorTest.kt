package community.flock.aigentic.core.workflow

import community.flock.aigentic.core.agent.WorkflowRun
import community.flock.aigentic.core.agent.test.util.TestData.finishedTaskWithResponseToolCall
import community.flock.aigentic.core.agent.test.util.TestData.stuckWithTaskToolCall
import community.flock.aigentic.core.agent.test.util.toModelResponse
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.dsl.thenProcess
import community.flock.aigentic.core.exception.AigenticException
import community.flock.aigentic.core.model.Model
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk

@AigenticParameter
data class WorkflowInput(val query: String)

@AigenticParameter
data class FirstResult(val firstProcessed: String)

@AigenticParameter
data class SecondResult(val secondProcessed: String)

@AigenticParameter
data class FinalResult(val output: String)

class WorkflowExecutorTest : DescribeSpec({

    describe("happy path") {

        it("should execute 2-agent workflow successfully") {

            val firstAgent =
                agent<WorkflowInput, FirstResult> {
                    model(createMockModel(FirstResult("middle result")))
                    task("Process input") {}
                }

            val secondAgent =
                agent<FirstResult, FinalResult> {
                    model(createMockModel(FinalResult("final result")))
                    task("Generate output") {}
                }

            val workflow = firstAgent thenProcess secondAgent
            val result = workflow.start(WorkflowInput("test"))

            result.outcome.shouldBeInstanceOf<Outcome.Finished<FinalResult>>()
            result.agentRuns[0].outcome.shouldBeInstanceOf<Outcome.Finished<FirstResult>>()
            result.agentRuns[1].outcome.shouldBeInstanceOf<Outcome.Finished<FinalResult>>()
            result.agentRuns.last() shouldBe result.agentRuns[1]

            verifyWorkflowResult(result, 2)
        }
    }

    describe("failure scenarios") {

        it("should handle 3-agent workflow when third agent fails") {

            val firstAgent =
                agent<WorkflowInput, FirstResult> {
                    model(createMockModel(FirstResult("first result")))
                    task("Process input") {}
                }

            val secondAgent =
                agent<FirstResult, SecondResult> {
                    model(createMockModel(SecondResult("second result")))
                    task("Process middle result") {}
                }

            val thirdAgent =
                agent<SecondResult, FinalResult> {
                    model(createMockModelWithStuck())
                    task("Generate final output") {}
                }

            val workflow = firstAgent thenProcess secondAgent thenProcess thirdAgent
            val result = workflow.start(WorkflowInput("test"))

            result.outcome.shouldBeInstanceOf<Outcome.Stuck>()
            result.agentRuns[0].outcome.shouldBeInstanceOf<Outcome.Finished<FirstResult>>()
            result.agentRuns[1].outcome.shouldBeInstanceOf<Outcome.Finished<SecondResult>>()
            result.agentRuns[2].outcome.shouldBeInstanceOf<Outcome.Stuck>()
            result.agentRuns.last() shouldBe result.agentRuns[2]

            verifyWorkflowResult(result, 3)
        }

        it("should stop at first agent when first agent encounters fatal error") {

            val firstAgent =
                agent<WorkflowInput, FirstResult> {
                    model(createMockModelWithException(AigenticException("First agent fatal error")))
                    task("Process input") {}
                }

            val secondAgent =
                agent<FirstResult, FinalResult> {
                    model(createMockModel(FinalResult("final result")))
                    task("Generate output") {}
                }

            val workflow = firstAgent thenProcess secondAgent
            val result = workflow.start(WorkflowInput("test"))

            result.outcome.shouldBeInstanceOf<Outcome.Fatal>()
            result.agentRuns[0].outcome.shouldBeInstanceOf<Outcome.Fatal>()
            (result.agentRuns[0].outcome as Outcome.Fatal).message shouldBe "First agent fatal error"
            result.agentRuns.last() shouldBe result.agentRuns[0]

            verifyWorkflowResult(result, 1)
        }
    }

    describe("5-agent workflows") {

        it("should execute 5-agent workflow successfully") {

            val firstAgent =
                agent<WorkflowInput, FirstResult> {
                    model(createMockModel(FirstResult("first result")))
                    task("Process input") {}
                }

            val secondAgent =
                agent<FirstResult, SecondResult> {
                    model(createMockModel(SecondResult("second result")))
                    task("Process first result") {}
                }

            val thirdAgent =
                agent<SecondResult, FirstResult> {
                    model(createMockModel(FirstResult("third result")))
                    task("Process second result") {}
                }

            val fourthAgent =
                agent<FirstResult, SecondResult> {
                    model(createMockModel(SecondResult("fourth result")))
                    task("Process third result") {}
                }

            val fifthAgent =
                agent<SecondResult, FinalResult> {
                    model(createMockModel(FinalResult("final result")))
                    task("Generate final output") {}
                }

            val workflow = firstAgent thenProcess secondAgent thenProcess thirdAgent thenProcess fourthAgent thenProcess fifthAgent
            val result = workflow.start(WorkflowInput("test"))

            result.outcome.shouldBeInstanceOf<Outcome.Finished<FinalResult>>()
            result.agentRuns[0].outcome.shouldBeInstanceOf<Outcome.Finished<FirstResult>>()
            result.agentRuns[1].outcome.shouldBeInstanceOf<Outcome.Finished<SecondResult>>()
            result.agentRuns[2].outcome.shouldBeInstanceOf<Outcome.Finished<FirstResult>>()
            result.agentRuns[3].outcome.shouldBeInstanceOf<Outcome.Finished<SecondResult>>()
            result.agentRuns[4].outcome.shouldBeInstanceOf<Outcome.Finished<FinalResult>>()
            result.agentRuns.last() shouldBe result.agentRuns[4]

            verifyWorkflowResult(result, 5)
        }
    }
})

private fun createMockModel(result: FirstResult): Model =
    mockk<Model>().also { mock ->
        coEvery { mock.sendRequest(any(), any()) } returnsMany
            listOf(
                finishedTaskWithResponseToolCall(result),
            ).toModelResponse()
    }

private fun createMockModel(result: SecondResult): Model =
    mockk<Model>().also { mock ->
        coEvery { mock.sendRequest(any(), any()) } returnsMany
            listOf(
                finishedTaskWithResponseToolCall(result),
            ).toModelResponse()
    }

private fun createMockModel(result: FinalResult): Model =
    mockk<Model>().also { mock ->
        coEvery { mock.sendRequest(any(), any()) } returnsMany
            listOf(
                finishedTaskWithResponseToolCall(result),
            ).toModelResponse()
    }

private fun createMockModelWithException(exception: Exception): Model =
    mockk<Model>().also { mock ->
        coEvery { mock.sendRequest(any(), any()) } throws exception
    }

fun createMockModelWithStuck(): Model {
    return mockk<Model>().also { mock ->
        coEvery { mock.sendRequest(any(), any()) } returnsMany
            listOf(
                stuckWithTaskToolCall,
            ).toModelResponse()
    }
}

fun verifyWorkflowResult(
    result: WorkflowRun<*>,
    expectedAgentCount: Int,
) {
    result.startedAt shouldBe result.agentRuns.first().startedAt
    result.finishedAt shouldBe result.agentRuns.last().finishedAt
    result.messages.size shouldBe result.agentRuns.flatMap { it.messages }.size
    result.modelRequests.size shouldBe result.agentRuns.flatMap { it.modelRequests }.size
    result.agentRuns.size shouldBe expectedAgentCount
}
