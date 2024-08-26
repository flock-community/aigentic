package community.flock.aigentic.platform.testing.model

import community.flock.aigentic.platform.testing.util.greenString
import community.flock.aigentic.platform.testing.util.redString

data class TestReport(
    val successes: Set<TestResult.Success>,
    val failures: Set<TestResult.Failed>,
    val errors: Set<TestResult.AgentError>,
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
        """.trimIndent(),
    )

private fun List<TestResult>.toTestReport() =
    fold(
        TestReport(emptySet(), emptySet(), emptySet()),
    ) { rapport, result ->
        when (result) {
            is TestResult.Success -> rapport.copy(successes = rapport.successes + result)
            is TestResult.Failed -> rapport.copy(failures = rapport.failures + result)
            is TestResult.AgentError -> rapport.copy(errors = rapport.errors + result)
        }
    }
