package community.flock.aigentic.core.tool

import community.flock.aigentic.core.agent.Attachment
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.message.Base64Content
import community.flock.aigentic.core.message.ToolResultContent
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@AigenticParameter
data class CropInput(
    val page: Int,
)

@AigenticParameter
data class CropOutput(
    val page: Int,
    val label: String,
)

@AigenticParameter
data class Crop(
    val page: Int,
    val image: Attachment,
)

@AigenticParameter
data class RenderedPage(
    val number: Int,
    val image: Attachment,
)

@AigenticParameter
data class Document(
    val name: String,
    val pages: List<RenderedPage>,
)

@AigenticParameter
data class Lookup(
    val found: Boolean,
    val photo: Attachment?,
)

class CreateToolTest :
    DescribeSpec({
        describe("createTool") {
            val arguments = buildJsonObject { put("page", 2) }
            val png = Base64Content("QUJD")

            it("answers a string as one text part") {
                createTool<CropInput, String>("Plain") { "ok" }.handler(arguments) shouldBe ToolResultContent("ok")
            }

            it("answers a number as one text part") {
                createTool<CropInput, Int>("Number") { it.page }.handler(arguments) shouldBe ToolResultContent("2")
            }

            it("answers an object as its json in one text part") {
                createTool<CropInput, CropOutput>("Object") { CropOutput(it.page, "cover") }.handler(arguments) shouldBe
                    ToolResultContent("""{"page":2,"label":"cover"}""")
            }

            it("replaces an attachment in the output with a reference and adds it to the attachments") {
                val image = Attachment.Base64.png("QUJD")

                createTool<CropInput, Crop>("Crop") { Crop(it.page, image) }.handler(arguments) shouldBe
                    ToolResultContent("""{"page":2,"image":"[attachment 1]"}""", listOf(image))
            }

            it("numbers the attachments in nested objects and lists in order") {
                val first = Attachment.Base64.png("QUJD")
                val second = Attachment.Url.jpeg("https://example.com/2.jpg")

                createTool<CropInput, Document>("Render") { Document("invoice", listOf(RenderedPage(1, first), RenderedPage(2, second))) }
                    .handler(arguments) shouldBe
                    ToolResultContent(
                        """{"name":"invoice","pages":[{"number":1,"image":"[attachment 1]"},{"number":2,"image":"[attachment 2]"}]}""",
                        listOf(first, second),
                    )
            }

            it("answers an attachment as the whole output") {
                val pdf = Attachment.Url.pdf("https://example.com/a.pdf")

                createTool<CropInput, Attachment>("Screenshot") { pdf }.handler(arguments) shouldBe
                    ToolResultContent("\"[attachment 1]\"", listOf(pdf))
            }

            it("answers a list of attachments") {
                val pages = listOf(Attachment.Base64.png("QUJD"), Attachment.Base64.png("REVG"))

                createTool<CropInput, List<Attachment>>("Pages") { pages }.handler(arguments) shouldBe
                    ToolResultContent("""["[attachment 1]","[attachment 2]"]""", pages)
            }

            it("keeps a missing optional attachment as null") {
                createTool<CropInput, Lookup>("Lookup") { Lookup(false, null) }.handler(arguments) shouldBe
                    ToolResultContent("""{"found":false,"photo":null}""")
            }

            it("answers a map with attachment values") {
                val front = Attachment.Base64.png("QUJD")

                createTool<CropInput, Map<String, Attachment>>("Map") { mapOf("front" to front) }.handler(arguments) shouldBe
                    ToolResultContent("""{"front":"[attachment 1]"}""", listOf(front))
            }

            it("passes tool result content through to the model") {
                val content = ToolResultContent("page 2", listOf(Attachment.Base64.png("QUJD")))

                createTool<CropInput, ToolResultContent>("Content") { content }.handler(arguments) shouldBe content
            }
        }

        describe("Base64Content") {
            it("strips a data url prefix") {
                Base64Content("data:image/png;base64,QUJD") shouldBe Base64Content("QUJD")
            }
        }
    })
