package community.flock.aigentic.core.agent

import community.flock.aigentic.core.agent.state.ModelRequestInfo
import community.flock.aigentic.core.agent.tool.Outcome
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class RunTest : DescribeSpec({
    describe("Run") {
        describe("tokenUsage()") {
            it("should correctly calculate token usage from model requests") {
                val run =
                    AgentRun(
                        startedAt = Instant.fromEpochMilliseconds(1000),
                        finishedAt = Instant.fromEpochMilliseconds(2000),
                        messages = emptyList(),
                        outcome = Outcome.Finished("Test completed", "Test result"),
                        modelRequests =
                            listOf(
                                ModelRequestInfo(
                                    startedAt = Instant.fromEpochMilliseconds(1100),
                                    finishedAt = Instant.fromEpochMilliseconds(1200),
                                    inputTokenCount = 10,
                                    outputTokenCount = 20,
                                    thinkingOutputTokenCount = 5,
                                    cachedInputTokenCount = 2,
                                ),
                                ModelRequestInfo(
                                    startedAt = Instant.fromEpochMilliseconds(1300),
                                    finishedAt = Instant.fromEpochMilliseconds(1400),
                                    inputTokenCount = 15,
                                    outputTokenCount = 25,
                                    thinkingOutputTokenCount = 8,
                                    cachedInputTokenCount = 3,
                                ),
                            ),
                        systemPromptMessage = community.flock.aigentic.core.message.Message.SystemPrompt("Test system prompt"),
                    )

                val tokenUsage = run.tokenUsage()

                tokenUsage.inputTokens shouldBe 25 // 10 + 15
                tokenUsage.outputTokens shouldBe 45 // 20 + 25
                tokenUsage.thinkingOutputTokens shouldBe 13 // 5 + 8
                tokenUsage.cachedInputTokens shouldBe 5 // 2 + 3
            }
        }
    }
})
