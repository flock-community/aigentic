@file:Suppress("ktlint:standard:max-line-length")

package community.flock.aigentic.example

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.openai.dsl.openAIModel
import community.flock.aigentic.openai.model.OpenAIModelIdentifier
import community.flock.aigentic.tools.openapi.dsl.openApiTools

suspend fun runOpenAPIAgent(
    openAIAPIKey: String,
    hackerNewsOpenAPISpec: String,
) {
    val run =
        agent<Unit, HackerNewsAgentResponse> {
            tags("validated")
            openAIModel {
                apiKey(openAIAPIKey)
                modelIdentifier(OpenAIModelIdentifier.GPT4O)
            }
            task("Send Hacker News stories about AI") {
                addInstruction("Retrieve the top 10 Hacker News stories")
                addInstruction("Send stories, if any, about AI to john@doe.com")
            }
            addTool<SendEmailRequest, SendEmailResponse>("sendEmail") {
                sendEmailHandler(it)
            }
            openApiTools(hackerNewsOpenAPISpec)
        }.start()

    when (val result = run.result) {
        is Result.Finished ->
            result.response.let { response ->
                "Hacker News agent completed successfully: $response"
            }

        is Result.Stuck -> "Agent is stuck and could not complete task, it says: ${result.reason}"
        is Result.Fatal -> "Agent crashed: ${result.message}"
    }.also(::println)
}

private fun sendEmailHandler(input: SendEmailRequest): SendEmailResponse {
    return SendEmailResponse("✉️ Sending email: '${input.message}' with subject: '${input.subject}' to recipient: ${input.emailAddress}")
}

@AigenticParameter
data class SendEmailRequest(
    val emailAddress: String,
    val subject: String,
    val message: String,
)

@AigenticParameter
data class SendEmailResponse(val message: String)

@AigenticParameter
data class HackerNewsAgentResponse(
    val storiesRetrieved: Int,
    val aiStoriesFound: Int,
    val emailsSent: List<String>,
)
