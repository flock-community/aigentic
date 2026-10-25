package community.flock.aigentic.openai.mapper

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.ListContent
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.openai.mapper.OpenAIMapper.toOpenAIMessage
import community.flock.aigentic.openai.model.OpenAIImageDetail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.be
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.beInstanceOf
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json

class OpenAIMapperTest :
    DescribeSpec({

        describe("OpenAI Mapper") {

            it("Should format data url when raw base64 content is provided") {
                val base64Content = "iVBORw0KGgoAAA=="
                val mimeType = MimeType.PNG
                val base64Message = Message.Base64(Sender.Model, base64Content, mimeType, MessageCategory.EXECUTION)

                val chatMessage = base64Message.toOpenAIMessage()

                chatMessage should beInstanceOf<ChatMessage>()
                chatMessage.role should be(ChatRole.Assistant)
                chatMessage.messageContent.shouldBeInstanceOf<ListContent>().run {
                    this.content[0].shouldBeInstanceOf<ImagePart>().run {
                        this.imageUrl.url should be("data:${mimeType.value};base64,$base64Content")
                    }
                }
            }

            it("should not format when already is base64 data url") {

                val base64Content = "data:image/png;base64,iVBORw0KGgoAAA=="
                val mimeType = MimeType.PNG
                val base64Message = Message.Base64(Sender.Model, base64Content, mimeType, MessageCategory.EXECUTION)

                val chatMessage = base64Message.toOpenAIMessage()

                chatMessage should beInstanceOf<ChatMessage>()
                chatMessage.role should be(ChatRole.Assistant)
                chatMessage.messageContent.shouldBeInstanceOf<ListContent>().run {
                    this.content[0].shouldBeInstanceOf<ImagePart>().run {
                        this.imageUrl.url should be(base64Content)
                    }
                }
            }

            it("should set the image detail on base64 and url images when configured") {
                listOf(
                    Message.Base64(Sender.Agent, "iVBORw0KGgoAAA==", MimeType.PNG, MessageCategory.EXECUTION),
                    Message.Url(Sender.Agent, "https://example.com/receipt.jpeg", MimeType.JPEG, MessageCategory.EXECUTION),
                ).forEach { message ->
                    message
                        .toOpenAIMessage(OpenAIImageDetail.ORIGINAL)
                        .messageContent
                        .shouldBeInstanceOf<ListContent>()
                        .content
                        .single()
                        .shouldBeInstanceOf<ImagePart>()
                        .imageUrl
                        .detail shouldBe "original"
                }
            }

            it("should map every image detail to its OpenAI value") {
                val base64Message = Message.Base64(Sender.Agent, "iVBORw0KGgoAAA==", MimeType.PNG, MessageCategory.EXECUTION)

                mapOf(
                    OpenAIImageDetail.AUTO to "auto",
                    OpenAIImageDetail.LOW to "low",
                    OpenAIImageDetail.HIGH to "high",
                    OpenAIImageDetail.ORIGINAL to "original",
                ).forEach { (imageDetail, detail) ->
                    base64Message
                        .toOpenAIMessage(imageDetail)
                        .messageContent
                        .shouldBeInstanceOf<ListContent>()
                        .content
                        .single()
                        .shouldBeInstanceOf<ImagePart>()
                        .imageUrl
                        .detail shouldBe detail
                }
            }

            it("should throw for base64 and url PDF attachments") {
                listOf(
                    Message.Base64(Sender.Agent, "JVBERi0=", MimeType.PDF, MessageCategory.RUN_CONTEXT),
                    Message.Url(Sender.Agent, "https://example.com/invoice.pdf", MimeType.PDF, MessageCategory.RUN_CONTEXT),
                ).forEach { message ->
                    shouldThrow<Exception> {
                        message.toOpenAIMessage()
                    }.message shouldContain "PDF attachments are not supported on OpenAI-compatible models"
                }
            }

            it("should not set an image detail when not configured") {
                Message
                    .Base64(Sender.Agent, "iVBORw0KGgoAAA==", MimeType.PNG, MessageCategory.EXECUTION)
                    .toOpenAIMessage()
                    .messageContent
                    .shouldBeInstanceOf<ListContent>()
                    .content
                    .single()
                    .shouldBeInstanceOf<ImagePart>()
                    .imageUrl
                    .detail shouldBe null
            }

            it("should add null to the type of optional tool parameters and list every parameter in required") {
                val tool =
                    object : ToolDescription {
                        override val name = ToolName("extract")
                        override val description: String? = null
                        override val parameters =
                            listOf(
                                Parameter.Primitive("name", null, isRequired = true, type = ParameterType.Primitive.String),
                                Parameter.Primitive("age", null, isRequired = false, type = ParameterType.Primitive.Integer),
                                Parameter.Complex.Enum(
                                    name = "unit",
                                    description = null,
                                    isRequired = false,
                                    default = PrimitiveValue.String("MM"),
                                    values = listOf(PrimitiveValue.String("MM"), PrimitiveValue.String("CM")),
                                    valueType = ParameterType.Primitive.String,
                                ),
                                Parameter.Complex.Object(
                                    name = "size",
                                    description = null,
                                    isRequired = false,
                                    parameters = listOf(Parameter.Primitive("value", null, isRequired = true, type = ParameterType.Primitive.Number)),
                                ),
                                Parameter.Complex.Array(
                                    name = "tags",
                                    description = null,
                                    isRequired = false,
                                    itemDefinition = Parameter.Primitive("Item", null, isRequired = true, type = ParameterType.Primitive.String),
                                ),
                            )
                    }

                tool
                    .toOpenAITool()
                    .function.parameters
                    ?.schema shouldBe
                    Json.parseToJsonElement(
                        """
                        {
                          "type": "object",
                          "properties": {
                            "name": { "type": "string" },
                            "age": { "type": ["integer", "null"] },
                            "unit": { "type": ["string", "null"], "enum": ["MM", "CM", null] },
                            "size": {
                              "type": ["object", "null"],
                              "properties": { "value": { "type": "number" } },
                              "required": ["value"]
                            },
                            "tags": { "type": ["array", "null"], "items": { "type": "string" } }
                          },
                          "required": ["name", "age", "unit", "size", "tags"]
                        }
                        """,
                    )
            }
        }
    })
