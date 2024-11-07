package community.flock.aigentic.platform.testing.model

import community.flock.aigentic.platform.testing.util.greenString
import community.flock.aigentic.platform.testing.util.redString

data class TestReport(
    val successes: Set<TestResult.Success>,
    val failures: Set<TestResult.Failed>,
    val errors: Set<TestResult.AgentError>,
    val inputTokenCount: Long,
    val outputTokenCount: Long,
) {
    companion object {
        fun from(results: List<TestResult>) = results.toTestReport()
    }
}

fun TestReport.prettyPrint() =
    println(
        """
        🏁 Test finished:

        🟢 Successes: ${successes.size.greenString()}
        🔴 Failures: ${failures.size.redString()}
        ❌ Errors: ${errors.size.redString()}

        Input token count: $inputTokenCount
        Output token count: $outputTokenCount
        """.trimIndent(),
    )

private fun List<TestResult>.toTestReport() =
    fold(
        TestReport(emptySet(), emptySet(), emptySet(), 0, 0),
    ) { rapport, result ->
        when (result) {
            is TestResult.Success -> {
                val inputTokens = result.state.modelRequestInfos.replayCache.sumOf { it.inputTokenCount }
                val outputTokens = result.state.modelRequestInfos.replayCache.sumOf { it.outputTokenCount }
                rapport.copy(
                    successes = rapport.successes + result,
                    inputTokenCount = rapport.inputTokenCount + inputTokens,
                    outputTokenCount = rapport.outputTokenCount + outputTokens,
                )
            }
            is TestResult.Failed -> rapport.copy(failures = rapport.failures + result)
            is TestResult.AgentError -> rapport.copy(errors = rapport.errors + result)
        }
    }
