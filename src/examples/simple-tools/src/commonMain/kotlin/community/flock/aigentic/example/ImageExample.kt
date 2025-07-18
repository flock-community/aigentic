package community.flock.aigentic.example

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.openai.dsl.openAIModel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier

@AigenticParameter
data class ItemName(
    val itemName: String,
)

@AigenticParameter
data class SaveItemResult(val message: String)

suspend fun runItemCategorizeExample(
    base64Image: String,
    apiKey: String,
) {
    val run =
        agent {
            openAIModel {
                apiKey(apiKey)
                modelIdentifier(OpenAIModelIdentifier.GPT4OMini)
            }
            task("Identify all items in the image and save each individual item") {}
            addTool("saveItem") { input: ItemName ->
                SaveItemResult("Successfully saved ${input.itemName}")
            }
            context {
                addBase64(base64Image, MimeType.JPEG)
            }
        }.start()

    when (val result = run.outcome) {
        is Outcome.Finished -> "Agent finished successfully"
        is Outcome.Stuck -> "Agent is stuck and could not complete task, it says: ${result.reason}"
        is Outcome.Fatal -> "Agent crashed: ${result.message}"
    }.also(::println)
}
