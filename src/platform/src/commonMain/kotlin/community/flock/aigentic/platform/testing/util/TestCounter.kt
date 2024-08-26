package community.flock.aigentic.platform.testing.util

import kotlinx.coroutines.coroutineScope

suspend fun <T> testCounter(
    totalNumberOfTest: Int,
    block: suspend (currentTestNumber: Int) -> T,
): T =
    coroutineScope {
        var testCounter = 1

        block(testCounter).also {
            testCounter++
        }
    }
