package community.flock.aigentic.example

import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        runWorkflowExample(
            geminiKey,
        )
    }
}
