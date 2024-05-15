@file:Suppress("ktlint:standard:max-line-length")

package community.flock.aigentic.example

import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.FinishReason.FinishedAllTasks
import community.flock.aigentic.core.agent.tool.FinishReason.ImStuck
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.Tool
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.core.tool.getIntValue
import community.flock.aigentic.core.tool.getStringValue
import community.flock.aigentic.dsl.openAIModel
import community.flock.aigentic.model.OpenAIModelIdentifier
import kotlinx.serialization.json.JsonObject

suspend fun runAdministrativeAgentExample(openAIAPIKey: String) {
    val run =
        agent {
            openAIModel(openAIAPIKey, OpenAIModelIdentifier.GPT4O)
            task("Retrieve all employees to inspect their hour status") {
                addInstruction(
                    "For all employees: only when the employee has not yet received 5 reminders to completed his hours send him a reminder through Signal. Base the tone of the message on the number of reminders sent",
                )
                addInstruction(
                    "If the employee is reminded 5 times and still has still not completed the hours don't send the employee a message but ask the manager on how to respond and send the manager's response to the employee",
                )
                addInstruction(
                    "When you for sure know that the signal message is successfully sent (which means the tool call returns a success response), make sure that you update the numberOfRemindersSent for each and every the specific employee.",
                )
            }
            addTool(getAllEmployeesOverviewTool)
            addTool(getEmployeeDetailByNameTool)
            addTool(askManagerForResponseTool)
            addTool(sendSignalMessageTool)
            addTool(updateEmployeeTool)
        }.start()

    when (run.result.reason) {
        FinishedAllTasks -> "Hours inspected successfully"
        ImStuck -> "Agent is stuck and could not complete task"
    }.also(::println)
}

val getAllEmployeesOverviewTool =
    object : Tool {
        override val name = ToolName("getAllEmployeesOverview")
        override val description = "Returns a list of all employees"
        override val parameters = emptyList<Parameter>()
        override val handler: suspend (map: JsonObject) -> String = {
            """
            |Employee: Niels
            |Telephone number: 0612345678

            |Employee: Henk
            |Telephone number: 0687654321

            |Employee: Jan
            |Telephone number: 0643211234
            """.trimMargin()
        }
    }

val getEmployeeDetailByNameTool =
    object : Tool {
        val nameParameter =
            Parameter.Primitive(
                "name",
                "The name of the employee",
                true,
                Primitive.String,
            )

        override val name = ToolName("getEmployeeDetailByName")
        override val description = "Returns the hour status of an employee by name"
        override val parameters = listOf(nameParameter)
        override val handler: suspend (map: JsonObject) -> String = {

            val name = nameParameter.getStringValue(it)

            when (name) {
                "Niels" ->
                    """
                    |Employee: Niels
                    |Telephone number: 0612345678
                    |Has completed hours: NO
                    |Number of reminders sent: 1
                    """.trimMargin()

                "Henk" ->
                    """
                    |Employee: Henk
                    |Telephone number: 0687654321
                    |Has completed hours: YES
                    |Number of reminders sent: 2
                    """.trimMargin()

                "Jan" ->
                    """
                    |Employee: Jan
                    |Telephone number: 0643211234
                    |Has completed hours: NO
                    |Number of reminders sent: 5
                    """.trimMargin()

                else -> "Unknown employee"
            }
        }
    }

val askManagerForResponseTool =
    object : Tool {
        val nameParameter =
            Parameter.Primitive(
                "name",
                "The name of the employee",
                true,
                Primitive.String,
            )

        override val name = ToolName("askManagerForResponse")
        override val description = "Ask to manager how to respond"
        override val parameters = listOf(nameParameter)
        override val handler: suspend (map: JsonObject) -> String = {

            val name = nameParameter.getStringValue(it)
            "$name, je moet nu echt je uren invullen anders word je ontslagen!"
        }
    }

val updateEmployeeTool =
    object : Tool {
        val nameParameter =
            Parameter.Primitive(
                "name",
                "The name of the employee",
                true,
                Primitive.String,
            )

        val numberOfRemindersSentParameter =
            Parameter.Primitive(
                "numberOfRemindersSent",
                "The updated value of the number of reminders sent to the employee",
                true,
                ParameterType.Primitive.Integer,
            )

        override val name = ToolName("updateEmployee")
        override val description = "Update the employee status"
        override val parameters = listOf(nameParameter, numberOfRemindersSentParameter)
        override val handler: suspend (map: JsonObject) -> String = {
            val name = nameParameter.getStringValue(it)
            val numberOfRemindersSent = numberOfRemindersSentParameter.getIntValue(it)
            "Updated number of reminders sent for '$name' to '$numberOfRemindersSent'"
        }
    }

val sendSignalMessageTool =
    object : Tool {
        val phoneNumberParam =
            Parameter.Primitive(
                "phoneNumber",
                "The telephone number of the receiver of this message",
                true,
                Primitive.String,
            )

        val messageParam =
            Parameter.Primitive(
                "message",
                null,
                true,
                Primitive.String,
            )

        override val name = ToolName("sendSignalMessage")
        override val description = "Sends a Signal message to the provided person"
        override val parameters = listOf(phoneNumberParam, messageParam)

        override val handler: suspend (JsonObject) -> String = { arguments ->

            val phoneNumber = phoneNumberParam.getStringValue(arguments)
            val message = messageParam.getStringValue(arguments)

            "✉️ Sending: '$message' to '$phoneNumber'"
        }
    }
