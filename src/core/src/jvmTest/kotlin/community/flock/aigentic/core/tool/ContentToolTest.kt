package community.flock.aigentic.core.tool

import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.ToolResultContent
import community.flock.aigentic.core.message.ToolResultPart
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@AigenticParameter
data class CropInput(
    val page: Int,
)

class ContentToolTest :
    DescribeSpec({
        describe("createContentTool") {
            val tool =
                createContentTool<CropInput>("Crop", "Crop a page") { input ->
                    ToolResultContent(listOf(ToolResultPart.Text("page ${input.page}"), ToolResultPart.Image("QUJD", MimeType.PNG)))
                }
            val arguments = buildJsonObject { put("page", 2) }

            it("returns text and image parts to the model") {
                tool.execute(arguments).parts shouldBe listOf(ToolResultPart.Text("page 2"), ToolResultPart.Image("QUJD", MimeType.PNG))
            }

            it("answers the text handler with a placeholder for the image") {
                tool.handler(arguments) shouldBe "page 2\n[image: image/png]"
            }

            it("keeps a plain tool's text as one text part") {
                val plain = createTool<CropInput, String>("Plain") { "ok" }
                plain.execute(arguments) shouldBe ToolResultContent("ok")
            }
        }
    })
