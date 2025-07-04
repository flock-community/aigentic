package community.flock.aigentic.example

import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        runItemCategorizeExample(
            FileReader.readFileBase64("/table-items.png"),
            openAIAPIKey,
        )
    }
}
