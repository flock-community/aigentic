package community.flock.aigentic.example

import community.flock.aigentic.code.generation.annotations.AigenticParameter
import community.flock.aigentic.code.generation.annotations.AigenticResponse
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.openai.dsl.openAIModel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier

@AigenticParameter
data class ItemName(
    val itemName: String
)

@AigenticResponse
data class SaveItemResult(val message: String)

suspend fun runItemCategorizeExample(
    base64Image: String,
    apiKey: String,
) {
    val run: Run =
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

    when (val result = run.result) {
        is community.flock.aigentic.core.agent.tool.Result.Finished -> "Agent finished successfully"
        is community.flock.aigentic.core.agent.tool.Result.Stuck -> "Agent is stuck and could not complete task, it says: ${result.reason}"
        is community.flock.aigentic.core.agent.tool.Result.Fatal -> "Agent crashed: ${result.message}"
    }.also(::println)
}
