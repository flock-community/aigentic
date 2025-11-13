package community.flock.aigentic.gemini.mapper

import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.model.GenerationSettings
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
            val base64Message = Message.Base64(Sender.Model, MessageCategory.EXECUTION, base64Content, mimeType)

            createGenerateContentRequest(listOf(base64Message), emptyList(), GenerationSettings.DEFAULT, null).contents[0].parts[0]
                .shouldBeInstanceOf<Part.Blob>().run {
                    this.inlineData shouldBe BlobContent(mimeType = mimeType.value, data = base64Content)
                }
        }

        it("should format when base64 data url is provided") {
            val base64Content = "data:image/png;base64,iVBORw0KGgoAAA=="
            val mimeType = MimeType.PNG
            val base64Message = Message.Base64(Sender.Model, MessageCategory.EXECUTION, base64Content, mimeType)

            createGenerateContentRequest(listOf(base64Message), emptyList(), GenerationSettings.DEFAULT, null).contents[0].parts[0]
                .shouldBeInstanceOf<Part.Blob>().run {
                    this.inlineData shouldBe BlobContent(mimeType = mimeType.value, data = "iVBORw0KGgoAAA==")
                }
        }
    }
})
