package community.flock.aigentic.koog

import ai.koog.prompt.message.AttachmentContent
import ai.koog.prompt.message.AttachmentSource
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageCategoryTest {
    @Test
    fun `categories are derived from where the message originates`() {
        val system = Message.System("You are helpful.", RequestMetaInfo.Empty)
        val configDocument =
            Message.User(
                listOf(MessagePart.Attachment(AttachmentSource.Image(AttachmentContent.URL("https://example.com/reference.png"), format = "png"))),
                RequestMetaInfo.Empty,
            )
        val runInput = Message.User("Process this document", RequestMetaInfo.Empty)
        val assistant = Message.Assistant("Processed", ResponseMetaInfo.Empty)

        // The config prompt is [system, configDocument]; the run appends [runInput] then the model replies.
        val dtos = listOf(system, configDocument, runInput, assistant).toMessageDtos(configPromptSize = 2)

        assertEquals(MessageCategoryDto.SYSTEM_PROMPT, dtos.filterIsInstance<SystemPromptMessageDto>().single().category)
        assertEquals(MessageCategoryDto.CONFIG_CONTEXT, dtos.filterIsInstance<UrlMessageDto>().single().category)
        assertEquals(
            MessageCategoryDto.RUN_CONTEXT,
            dtos.filterIsInstance<TextMessageDto>().single { it.sender == SenderDto.Agent }.category,
        )
        assertEquals(
            MessageCategoryDto.EXECUTION,
            dtos.filterIsInstance<TextMessageDto>().single { it.sender == SenderDto.Model }.category,
        )
    }

    @Test
    fun `an explicit metadata category overrides the positional default`() {
        val tagged =
            Message.User(
                listOf(MessagePart.Text("An example exchange")),
                RequestMetaInfo.Empty.copy(metadata = buildJsonObject { put(CATEGORY_METADATA_KEY, "EXAMPLE") }),
            )

        val dtos = listOf(tagged).toMessageDtos(configPromptSize = 0)

        assertEquals(MessageCategoryDto.EXAMPLE, dtos.filterIsInstance<TextMessageDto>().single().category)
    }
}
