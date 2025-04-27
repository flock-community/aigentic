package community.flock.aigentic.example

import community.flock.aigentic.core.Aigentic
import community.flock.aigentic.generated.parameter.initialize
import kotlinx.coroutines.runBlocking

object AdministrativeAgentExample {

    @JvmStatic
    fun main(args: Array<String>) {
        Aigentic.initialize()
        runBlocking {
            runAdministrativeAgentExample(openAIAPIKey)
        }
    }
}

object InvoiceExtractorExample {

    @JvmStatic
    fun main(args: Array<String>) {
        Aigentic.initialize()
        runBlocking {
            invoiceExtractorAgent(
                FileReader.readFileBase64("/test-invoice.pdf"),
                geminiKey
            )
        }
    }
}

object ItemCategorizeExample {

    @JvmStatic
    fun main(args: Array<String>) {
        Aigentic.initialize()
        runBlocking {
            runItemCategorizeExample(
                FileReader.readFileBase64("/table-items.png"),
                openAIAPIKey
            )
        }
    }
}

object KotlinMessageSenderExample {

    @JvmStatic
    fun main(args: Array<String>) {
        Aigentic.initialize()
        runBlocking {
            runKotlinMessageAgent(openAIAPIKey)
        }
    }
}



