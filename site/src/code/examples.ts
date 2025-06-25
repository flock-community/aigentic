export const agent = `fun agent(question: String) = agent {

  @AigenticResponse
  data class Answer(val answer: String)

  // Configure the model for the agent
  openAIModel {
      apiKey("YOUR_API_KEY")
      modelIdentifier(OpenAIModelIdentifier.GPT4Turbo)
  }

  // Configure the task for the agent
  task("Answer questions about Kotlin Multiplatform") {
      addInstruction("Provide concise and accurate answers")
  }

  // Set context
  context {
      addText(question)
  }

  finishResponse<Answer>()
}

// Start the agent and get a run
val run = agent("What is cool about kotlin?").start()

// Print the result
when (val result = run.result) {
  is Result.Finished -> println(result.getFinishResponse<Answer>()?.answer)
  is Result.Stuck -> println("Agent is stuck: \${result.reason}")
  is Result.Fatal -> println("Error: \${result.message}")
}

println("""
     Token Usage:
    - Input tokens: \${run.inputTokens()}
    - Output tokens: \${run.outputTokens()}
    - Thinking output tokens: \${run.thinkingOutputTokens()}
    - Cached input tokens: \${run.cachedInputTokens()}
""")

`
