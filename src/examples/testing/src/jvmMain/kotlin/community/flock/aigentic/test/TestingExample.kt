package community.flock.aigentic.test

import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.PrimitiveValue
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import community.flock.aigentic.platform.dsl.platform
import community.flock.aigentic.platform.dsl.regressionTest
import community.flock.aigentic.platform.testing.start
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
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

//            //val name = licencePlates.getStringValue(arguments)
//            Json.encodeToString(SavedItemResponse(UUID.randomUUID().toString(), "Saved $arguments successfully"))
            error("I shouldn't be called")
        }
    }

@Serializable
data class SavedItemResponse(
    val id: String,
    val status: String = "saved successfully",
)

private val geminiKey by lazy {
    System.getenv("GEMINI_API_KEY").also {
        if (it.isNullOrEmpty()) error("Set 'GEMINI_API_KEY' environment variable!")
    }
}

val saveNewsEventSentimentTool =
    object : Tool {
        override val name = ToolName("saveNewsEventSentiment")
        override val description = "Saves the news event components"

        val titleParameter =
            Parameter.Primitive(
                name = "title",
                description = "The title of the news event",
                isRequired = true,
                type = ParameterType.Primitive.String,
            )

        val sentimentParameter =
            Parameter.Complex.Enum(
                "sentiment",
                "The sentiment of the news event",
                true,
                default = null,
                values =
                    listOf(
                        PrimitiveValue.String("positive"),
                        PrimitiveValue.String("negative"),
                        PrimitiveValue.String("neutral"),
                    ),
                valueType = ParameterType.Primitive.String,
            )

        override val parameters = listOf(titleParameter, sentimentParameter)

        override val handler: suspend (JsonObject) -> String = { arguments ->
            error("I shouldn't be called")
        }
    }

val newsfeedAgent =
    agent {
        geminiModel {
            apiKey(geminiKey)
            modelIdentifier(GeminiModelIdentifier.Gemini1_5FlashLatest)
        }
        task("Summarize the newsfeed of the day and determine the sentiment") {
            addInstruction("Analyze the sentiment for each news event")
            addInstruction("Save the sentiment for each news event")
        }
        addTool(saveNewsEventSentimentTool)
        platform {
            name("newsfeed-agent")
            secret("36b2f9db-27c1-4dda-956b-163e572f22c6")
            apiUrl("http://localhost:8080")
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
            numberOfIterations(2)
            addTag("validated")
            agent(licencePlateExtractor)
        }.start()
    }
