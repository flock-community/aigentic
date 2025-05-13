package community.flock.aigentic.example

import community.flock.aigentic.core.Aigentic
import community.flock.aigentic.generated.parameter.initialize
import kotlinx.coroutines.runBlocking

fun main() {
    Aigentic.initialize()
    runBlocking {
        runKotlinMessageAgent(openAIAPIKey)
    }
}
