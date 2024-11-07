package community.flock.aigentic.test

import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import community.flock.aigentic.platform.dsl.platform
import community.flock.aigentic.platform.dsl.regressionTest
import community.flock.aigentic.platform.testing.start
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject

val invoiceExtractTool =
    object : Tool {
        val invoiceNumber =
            Parameter.Primitive(
                "invoiceNumber",
                null,
                true,
                Primitive.String,
            )

        val customerNumber =
            Parameter.Primitive(
                "debtorNumber",
                "The customer number",
                true,
                Primitive.String,
            )

        val invoiceTotal =
            Parameter.Primitive(
                "invoiceTotal",
                "The total amount of the invoice",
                true,
                Primitive.String,
            )

        val licencePlates =
            Parameter.Complex.Array(
                "licencePlates",
                "A list of the licenceplates in the invoice",
                true,
                itemDefinition =
                    Parameter.Primitive(
                        "licencePlate",
                        "The licence plate",
                        true,
                        Primitive.String,
                    ),
            )

        override val name = ToolName("saveInvoiceTool")
        override val description = "Saves the invoice details"
        override val parameters = listOf(invoiceNumber, customerNumber, licencePlates, invoiceTotal)

        override val handler: suspend (JsonObject) -> String = { arguments ->

            error("I shouldn't be called")
        }
    }

private val geminiKey by lazy {
    System.getenv("GEMINI_API_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'GEMINI_API_KEY' environment variable!")
    }
}

val licencePlateExtractor =
    agent {
        task("Extract the invoice elements from the provided document") {}
        addTool(invoiceExtractTool)
        geminiModel {
            apiKey(geminiKey)
            modelIdentifier(GeminiModelIdentifier.Gemini1_5FlashLatest)
        }
        platform {
            name("licence-plate-extractor")
            secret("ac5eff83-21bd-449d-9dc1-5817d5e1e8f8")
            apiUrl("http://localhost:8080")
        }
        finishResponse(
            Parameter.Primitive(
                "Explanation",
                "Please explain the provided document as accurate as possible",
                true,
                Primitive.String,
            ),
        )
    }

fun main(): Unit =
    runBlocking {
        regressionTest {
            agent(licencePlateExtractor)
            tags("validated")
            numberOfIterations(2)
        }.start()
    }
