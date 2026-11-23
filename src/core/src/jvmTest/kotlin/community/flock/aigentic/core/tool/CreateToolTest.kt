package community.flock.aigentic.core.tool

import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.message.Base64Content
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

@AigenticParameter
data class CropOutput(
    val page: Int,
    val label: String,
)

class CreateToolTest :
    DescribeSpec({
        describe("createTool") {
            val arguments = buildJsonObject { put("page", 2) }

            it("passes text and image parts through to the model") {
                val tool =
                    createTool<CropInput, ToolResultContent>("Crop", "Crop a page") { input ->
                        ToolResultContent(listOf(ToolResultPart.Text("page ${input.page}"), ToolResultPart.Image(Base64Content("QUJD"), MimeType.PNG)))
                    }

                tool.handler(arguments).parts shouldBe listOf(ToolResultPart.Text("page 2"), ToolResultPart.Image(Base64Content("QUJD"), MimeType.PNG))
            }

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
        }

        describe("Base64Content") {
            it("strips a data url prefix") {
                Base64Content("data:image/png;base64,QUJD") shouldBe Base64Content("QUJD")
            }
        }
    })
