package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.agent.Context
import community.flock.aigentic.core.agent.message.SystemPromptBuilder
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.model.Model
import community.flock.aigentic.core.tool.Tool
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class AgentConfigTest : DescribeSpec({

    describe("AgentConfig") {

        it("should build basic agent") {
            val model = mockk<Model>(relaxed = true)
            agent {
                name("agent-name")
                model(model)
                task("Task description") {
                    addInstruction("Instruction description")
                }
                addTool(mockk(relaxed = true))
            }.run {
                name shouldBe "agent-name"
                model shouldBe model
                task.description shouldBe "Task description"
                task.instructions.size shouldBe 1
                task.instructions.first().text shouldBe "Instruction description"
            }
        }

        it("should build agent with multiple tools") {
            val tool1 = mockk<Tool>(relaxed = true)
            val tool2 = mockk<Tool>(relaxed = true)

            agent {
                name("agent-name")
                model(mockk(relaxed = true))
                task("Task description") {}
                addTool(tool1)
                addTool(tool2)
            }.run {
                tools shouldBe mapOf(tool1.name to tool1, tool2.name to tool2)
            }
        }

        it("should build agent with system prompt builder") {
            val systemPromptBuilder = mockk<SystemPromptBuilder>(relaxed = true)
            agent {
                name("agent-name")
                model(mockk(relaxed = true))
                task("Task description") {}
                systemPrompt(systemPromptBuilder)
                addTool(mockk(relaxed = true))
            }.run {
                systemPromptBuilder shouldBe systemPromptBuilder
            }
        }

        it("should build agent with multiple contexts") {
            agent {
                name("agent-name")
                model(mockk(relaxed = true))
                task("Task description") {}
                context {
                    addText("Some text")
                    addImageUrl("https://example.com/image.jpg", MimeType.JPEG)
                }
                addTool(mockk(relaxed = true))
            }.run {
                contexts.size shouldBe 2
                contexts.first() shouldBe Context.Text("Some text")
                contexts.last() shouldBe Context.ImageUrl("https://example.com/image.jpg", MimeType.JPEG)
            }
        }

        withData(
            nameFn = { "Should succeed with valid name $it" },
            "test-agent",
            "0299agent",
            "agent0292",
        ) {

            agent {
                name(it)
                model(mockk(relaxed = true))
                task("Task description") {}
                addTool(mockk(relaxed = true))
            }
        }

        withData(
            nameFn = { "Should fail with invalid name $it" },
            "Test-agent",
            "0299_agent",
            "agent0!!292",
        ) {
            shouldThrow<IllegalArgumentException> {
                agent {
                    name(it)
                    model(mockk(relaxed = true))
                    task("Task description") {}
                    addTool(mockk(relaxed = true))
                }
            }.run {
                message shouldBe "Agent name can only contain lowercase letters, numbers and dashes"
            }
        }

        withData(
            nameFn = { "Should fail with: '${it.expectedMessage}'" },
            MissingPropertyTestCase(
                agentConfig = {
                },
                expectedMessage = "Cannot build Agent, property 'name' is missing, please use 'name()' to provide it",
            ),
            MissingPropertyTestCase(
                agentConfig = {
                    name("agent-name")
                    task("Task description") {}
                },
                expectedMessage = "Cannot build Agent, property 'model' is missing, please use 'model()' to provide it",
            ),
            MissingPropertyTestCase(
                agentConfig = {
                    name("agent-name")
                    model(mockk(relaxed = true))
                },
                expectedMessage = "Cannot build Agent, property 'task' is missing, please use 'task()' to provide it",
            ),
            MissingPropertyTestCase(
                agentConfig = {
                    name("agent-name")
                    model(mockk(relaxed = true))
                    task("Task description") {}
                },
                expectedMessage = "Cannot build Agent, property 'tools' is missing, please use 'addTool()' to provide it",
            ),
        ) { testCase ->
            shouldThrow<IllegalStateException> {
                agent(testCase.agentConfig)
            }.run {
                message shouldBe testCase.expectedMessage
            }
        }
    }
})

private data class MissingPropertyTestCase(val agentConfig: AgentConfig.() -> Unit, val expectedMessage: String)
