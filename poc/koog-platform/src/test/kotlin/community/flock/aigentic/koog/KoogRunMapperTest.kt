package community.flock.aigentic.koog

import ai.koog.prompt.message.AttachmentContent
import ai.koog.prompt.message.AttachmentSource
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KoogRunMapperTest {
    @Test
    fun `maps the supported aigentic message types from koog messages`() {
        val system = Message.System("You are helpful.", RequestMetaInfo.Empty)
        val user =
            Message.User(
                parts =
                    listOf(
                        MessagePart.Text("Describe these"),
                        MessagePart.Attachment(AttachmentSource.Image(AttachmentContent.URL("https://example.com/cat.png"), format = "png")),
                        MessagePart.Attachment(AttachmentSource.File(AttachmentContent.Binary.Base64("JVBERi0="), format = "pdf", mimeType = "application/pdf")),
                    ),
                metaInfo = RequestMetaInfo.Empty,
            )
        val assistant =
            Message.Assistant(
                parts =
                    listOf(
                        MessagePart.Text("Here you go"),
                        MessagePart.Tool.Call(id = "call-1", tool = "lookup", args = """{"q":"x"}"""),
                    ),
                metaInfo = ResponseMetaInfo.Empty,
            )
        val toolResult =
            Message.User(
                parts = listOf(MessagePart.Tool.Result(id = "call-1", tool = "lookup", output = "found")),
                metaInfo = RequestMetaInfo.Empty,
            )

        val dtos = listOf(system, user, assistant, toolResult).toMessageDtos()

        assertTrue(dtos.any { it is SystemPromptMessageDto })
        assertTrue(dtos.any { it is TextMessageDto && it.sender == SenderDto.Agent && it.text == "Describe these" })
        assertTrue(dtos.any { it is TextMessageDto && it.sender == SenderDto.Model && it.text == "Here you go" })

        val url = dtos.filterIsInstance<UrlMessageDto>().single()
        assertEquals("https://example.com/cat.png", url.url)
        assertEquals(MimeTypeDto.IMAGE_PNG, url.mimeType)

        val base64 = dtos.filterIsInstance<Base64MessageDto>().single()
        assertEquals("JVBERi0=", base64.base64Content)
        assertEquals(MimeTypeDto.APPLICATION_PDF, base64.mimeType)

        val toolCalls = dtos.filterIsInstance<ToolCallsMessageDto>().single()
        assertEquals("lookup", toolCalls.toolCalls.single().name)

        val toolResultDto = dtos.filterIsInstance<ToolResultMessageDto>().single()
        assertEquals("found", toolResultDto.response)
    }

    @Test
    fun `skips attachments whose mime type the platform contract does not support`() {
        val user =
            Message.User(
                parts = listOf(MessagePart.Attachment(AttachmentSource.Video(AttachmentContent.URL("https://example.com/clip.mp4"), format = "mp4"))),
                metaInfo = RequestMetaInfo.Empty,
            )

        val dtos = listOf(user).toMessageDtos()

        assertTrue(dtos.none { it is UrlMessageDto || it is Base64MessageDto })
    }
}
