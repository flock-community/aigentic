package community.flock.aigentic.openai.mapper

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.ListContent
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.openai.mapper.OpenAIMapper.toOpenAIMessage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.be
import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import io.kotest.matchers.types.shouldBeInstanceOf

class OpenAIMapperTest : DescribeSpec({

    describe("OpenAI Mapper") {

        it("Should format data url when raw base64 content is provided") {
            val base64Content = "iVBORw0KGgoAAA=="
            val mimeType = MimeType.PNG
            val imageBase64Message = Message.ImageBase64(Sender.Model, base64Content, mimeType)

            val chatMessage = imageBase64Message.toOpenAIMessage()

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
            val imageBase64Message = Message.ImageBase64(Sender.Model, base64Content, mimeType)

            val chatMessage = imageBase64Message.toOpenAIMessage()

            chatMessage should beInstanceOf<ChatMessage>()
            chatMessage.role should be(ChatRole.Assistant)
            chatMessage.messageContent.shouldBeInstanceOf<ListContent>().run {
                this.content[0].shouldBeInstanceOf<ImagePart>().run {
                    this.imageUrl.url should be(base64Content)
                }
            }
        }
    }
})
