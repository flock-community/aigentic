package community.flock.aigentic.example

import community.flock.aigentic.core.agent.InputData
import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.dsl.AgentConfig
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getItems
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

val saveInvoiceComponents =
    object : Tool {
        val invoiceComponents =
            Parameter.Complex.Array(
                name = "components",
                description = "A list of the invoice components like number, date, customer_number, etc",
                isRequired = true,
                itemDefinition =
                    Parameter.Complex.Object(
                        name = "component",
                        description = null,
                        isRequired = true,
                        parameters =
                            listOf(
                                Parameter.Primitive(
                                    name = "name",
                                    description = "The name of the invoice component e.g. number or date",
                                    isRequired = true,
                                    type = Primitive.String,
                                ),
                                Parameter.Primitive(
                                    name = "value",
                                    description = "The value of the component",
                                    isRequired = true,
                                    type = Primitive.String,
                                ),
                            ),
                    ),
            )

        override val name = ToolName("saveInvoiceComponents")
        override val description = "Saves the individual invoice components"
        override val parameters = listOf(invoiceComponents)

        override val handler: suspend (JsonObject) -> String = { arguments ->
            val components = invoiceComponents.getItems<InvoiceComponent>(arguments)
            Json.encodeToString("Saved ${components.size} invoice components successfully")
        }
    }

@Serializable
data class InvoiceComponent(
    val name: String,
    val value: String,
)

suspend fun invoiceExtractorAgent(
    invoicePdfBase64: String,
    configureModel: AgentConfig.() -> Unit,
): Run {
    val agent =
        agent {
            configureModel()
            task("Extract the different invoice components") {
                addInstruction("Please provide list of the invoice components")
            }
            addTool(saveInvoiceComponents)
        }

    return agent.start(listOf(InputData.Base64(invoicePdfBase64, MimeType.PDF)))
}
