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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// val saveInvoiceComponents =
//    object : Tool {
//        val invoiceComponents =
//            Parameter.Complex.Array(
//                name = "components",
//                description = "A list of the invoice components like number, date, customer_number, etc",
//                isRequired = true,
//                itemDefinition =
//                    Parameter.Complex.Object(
//                        name = "component",
//                        description = null,
//                        isRequired = true,
//                        parameters =
//                            listOf(
//                                Parameter.Primitive(
//                                    name = "name",
//                                    description = "The name of the invoice component e.g. number or date",
//                                    isRequired = true,
//                                    type = Primitive.String,
//                                ),
//                                Parameter.Primitive(
//                                    name = "value",
//                                    description = "The value of the component",
//                                    isRequired = true,
//                                    type = Primitive.String,
//                                ),
//                            ),
//                    ),
//            )
//
//        override val name = ToolName("saveInvoiceComponents")
//        override val description = "Saves the individual invoice components"
//        override val parameters = listOf(invoiceComponents)
//
//        override val handler: suspend (JsonObject) -> String = { arguments ->
//            val components = invoiceComponents.getItems<InvoiceComponent>(arguments)
//            Json.encodeToString("Saved ${components.size} invoice components successfully")
//        }
//    }

val invoiceLineDescription =
    Parameter.Complex.Object(
        "InvoiceLine",
        isRequired = false,
        description = "invoice line with a description and cost amount",
        parameters =
            listOf(
                Parameter.Primitive(
                    name = "amount",
                    description = null,
                    isRequired = true,
                    Primitive.String,
                ),
                Parameter.Primitive(
                    name = "description",
                    description = null,
                    isRequired = true,
                    Primitive.String,
                ),
            ),
    )

val agentInvoiceResponse =
    Parameter.Complex.Object(
        "response",
        isRequired = true,
        description = "invoice containing data on costs",
        parameters =
            listOf(
                Parameter.Primitive(
                    name = "date",
                    description = null,
                    isRequired = true,
                    Primitive.String,
                ),
                Parameter.Primitive(
                    name = "totalAmountExcludingTax",
                    description = null,
                    isRequired = true,
                    Primitive.Number,
                ),
                Parameter.Primitive(
                    name = "totalAmountIncludingTax",
                    description = null,
                    isRequired = true,
                    Primitive.Number,
                ),
                Parameter.Primitive(
                    name = "totalTaxAmount",
                    description = null,
                    isRequired = true,
                    Primitive.Number,
                ),
                Parameter.Primitive(
                    name = "taxRate",
                    description = null,
                    isRequired = true,
                    Primitive.Number,
                ),
                Parameter.Complex.Array(
                    name = "invoiceLines",
                    description = null,
                    isRequired = true,
                    itemDefinition = invoiceLineDescription,
                ),
            ),
    )

val invoiceExtractTool =
    object : Tool {
        override val name = ToolName("saveInvoiceTool")
        override val description = "Saves the invoice details"
        override val parameters = listOf(agentInvoiceResponse)

        override val handler: suspend (JsonObject) -> String = { arguments ->
            // val name = invoiceNumber.getStringValue(arguments)
            "save success"
        }
    }

@Serializable
data class InvoiceComponent(
    val name: String,
    val value: String,
)

val extractTextTool =
    object : Tool {
        val textParam =
            Parameter.Primitive(
                name = "date",
                description = null,
                isRequired = true,
                Primitive.String,
            )

        override val name = ToolName("extractTextTool")
        override val description = "Saves the extracted text"
        override val parameters = listOf(textParam)

        override val handler: suspend (JsonObject) -> String = { arguments ->
            // val name = invoiceNumber.getStringValue(arguments)
            "save success"
        }
    }

suspend fun invoiceExtractorAgent(
    invoicePdfBase64: String,
    configureModel: AgentConfig.() -> Unit,
): Run {
    val run =
        agent {
            configureModel()
            task("Extract components from the text in the provided image") {
                addInstruction("save the extracted component text")
            }
            addTool(extractTextTool)
//            task("Extract the different invoice components") {
//                addInstruction("Please provide list of the invoice components")
//            }
//            addTool(invoiceExtractTool)

            context {
                addBase64(invoicePdfBase64, MimeType.PNG)
            }
        }.start()

    return run
}
