package community.flock.aigentic.gemini.mapper

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.gemini.client.model.BlobContent
import community.flock.aigentic.gemini.client.model.Part
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class GeminiRequestMapperKtTest : DescribeSpec({

    describe("Gemini Request Mapper") {

        it("Should not format when raw base64 content is provided") {
            val base64Content = "iVBORw0KGgoAAA=="
            val mimeType = MimeType.PNG
            val imageBase64Message = Message.ImageBase64(Sender.Model, base64Content, mimeType)

            createGenerateContentRequest(listOf(imageBase64Message), emptyList()).contents[0].parts[0]
                .shouldBeInstanceOf<Part.Blob>().run {
                    this.inlineData shouldBe BlobContent(mimeType = mimeType.value, data = base64Content)
                }
        }

        it("should format when base64 data url is provided") {
            val base64Content = "data:image/png;base64,iVBORw0KGgoAAA=="
            val mimeType = MimeType.PNG
            val imageBase64Message = Message.ImageBase64(Sender.Model, base64Content, mimeType)

            createGenerateContentRequest(listOf(imageBase64Message), emptyList()).contents[0].parts[0]
                .shouldBeInstanceOf<Part.Blob>().run {
                    this.inlineData shouldBe BlobContent(mimeType = mimeType.value, data = "iVBORw0KGgoAAA==")
                }
        }
    }
})
