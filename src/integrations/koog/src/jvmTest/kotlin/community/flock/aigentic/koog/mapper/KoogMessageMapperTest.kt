package community.flock.aigentic.koog.mapper

import ai.koog.prompt.Prompt
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.message.ToolCall
import community.flock.aigentic.core.message.ToolCallId
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import ai.koog.prompt.message.Message as KoogMessage
import community.flock.aigentic.core.message.Message as AigenticMessage

class KoogMessageMapperTest :
    DescribeSpec({

        val responseMetaInfo = ResponseMetaInfo(timestamp = Instant.DISTANT_PAST, inputTokensCount = 5, outputTokensCount = 3)

        describe("Prompt mapping") {

            it("extracts the system prompt text and the first user message") {
                val prompt =
                    Prompt(
                        messages =
                            listOf(
                                KoogMessage.System("You are a weather assistant.", RequestMetaInfo.Empty),
                                KoogMessage.User("What's the weather in Amsterdam?", RequestMetaInfo.Empty),
                            ),
                        id = "test",
                    )

                prompt.systemPromptText() shouldBe "You are a weather assistant."
                prompt.initialUserText() shouldBe "What's the weather in Amsterdam?"
            }
        }

        describe("Assistant message mapping") {

            it("maps a tool call response to Aigentic ToolCalls") {
                val assistant =
                    KoogMessage.Assistant(
                        part = MessagePart.Tool.Call(id = "call_1", tool = "get_weather", args = """{"city":"Amsterdam"}"""),
                        metaInfo = responseMetaInfo,
                    )

                val mapped = assistant.toAigenticMessages()

                mapped shouldBe
                    listOf(
                        AigenticMessage.ToolCalls(
                            toolCalls =
                                listOf(
                                    ToolCall(
                                        id = ToolCallId("call_1"),
                                        name = "get_weather",
                                        arguments = """{"city":"Amsterdam"}""",
                                    ),
                                ),
                        ),
                    )
            }

            it("maps a plain text response to Aigentic Text") {
                val assistant = KoogMessage.Assistant(content = "It's sunny in Amsterdam.", metaInfo = responseMetaInfo)

                val mapped = assistant.toAigenticMessages()

                mapped shouldBe
                    listOf(
                        AigenticMessage.Text(sender = Sender.Model, text = "It's sunny in Amsterdam.", category = MessageCategory.EXECUTION),
                    )
            }

            it("maps a blank text response to no messages") {
                val assistant = KoogMessage.Assistant(content = "", metaInfo = responseMetaInfo)

                assistant.toAigenticMessages() shouldBe emptyList()
            }
        }
    })
