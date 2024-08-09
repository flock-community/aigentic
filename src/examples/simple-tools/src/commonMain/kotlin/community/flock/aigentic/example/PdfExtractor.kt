package community.flock.aigentic.example

import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getStringValue
import kotlinx.serialization.json.JsonObject

val savePdfSummary =
    object : Tool {
        val title =
            Parameter.Primitive(
                name = "title",
                description = null,
                isRequired = true,
                type = Primitive.String,
            )

        val mainPointsParameter =
            Parameter.Complex.Array(
                name = "mainPoints",
                description = "List of main points mentioned in the article",
                isRequired = true,
                itemDefinition =
                    Parameter.Primitive(
                        name = "mainPoint",
                        description = null,
                        isRequired = true,
                        type = Primitive.String,
                    ),
            )

        override val name = ToolName("savePdfSummary")
        override val description = "Saves the summary of the PDF"
        override val parameters = listOf(title, mainPointsParameter)

        override val handler: suspend (JsonObject) -> String = { arguments ->

            val message = title.getStringValue(arguments)
            "Successfully saved: '$message' "
        }
    }

suspend fun pdfSummaryAgent(
    pdfBase64: String,
    configureModel: AgentConfig.() -> Unit,
): Run {
    val run =
        agent {
            configureModel()
            task("Summarize the content of a PDF") {
                addInstruction("Give the summary a comprehensive title")
                addInstruction("Please provide list of main points")
            }
            addTool(savePdfSummary)
            context {
                addBase64(pdfBase64, MimeType.PDF)
            }
        }.start()

    return run
}
