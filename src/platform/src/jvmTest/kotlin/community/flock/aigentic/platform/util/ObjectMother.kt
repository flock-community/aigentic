package community.flock.aigentic.platform.util

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.AgentRun
import community.flock.aigentic.core.agent.Instruction
import community.flock.aigentic.core.agent.Task
import community.flock.aigentic.core.agent.message.DefaultSystemPromptBuilder
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.model.ModelIdentifier
import community.flock.aigentic.core.model.ModelResponse
import community.flock.aigentic.core.tool.ToolDescription
import kotlin.time.Clock

fun createAgent() =
    Agent<Unit, String>(
        platform = null,
        systemPromptBuilder = DefaultSystemPromptBuilder,
        model =
            object : Model {
                override val modelIdentifier: ModelIdentifier =
                    object : ModelIdentifier {
                        override val stringValue: String = "test-model-identifier"
                    }
                override val generationSettings = GenerationSettings.DEFAULT

                override suspend fun sendRequest(
                    messages: List<Message>,
                    tools: List<ToolDescription>,
                ): ModelResponse {
                    TODO("Not yet implemented")
                }
            },
        task =
            Task(
                description = "description",
                instructions = listOf(Instruction("Some instruction")),
            ),
        contexts = emptyList(),
        tools = emptyMap(),
        tags = emptyList(),
    )

fun createAgentRun() =
    AgentRun<String>(
        startedAt = Clock.System.now(),
        finishedAt = Clock.System.now(),
        messages = listOf(Message.SystemPrompt("You are a helpful agent")),
        modelRequests = emptyList(),
        outcome =
            Outcome.Finished(
                description = "description",
                response = "response",
            ),
    )
