@file:Suppress("ktlint:standard:max-line-length")

package community.flock.aigentic.example

import community.flock.aigentic.core.agent.Run
import community.flock.aigentic.core.agent.getFinishResponse
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.Result
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.annotations.AigenticResponse
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier

suspend fun runAdministrativeAgentExample(apiKey: String) {
    val run: Run =
        agent {
            geminiModel {
                apiKey(apiKey)
                modelIdentifier(GeminiModelIdentifier.Gemini2_0Flash)
            }
            task("Retrieve all employees to inspect their hour status") {
                addInstruction(
                    "For all employees: only when the employee has not yet received 5 reminders to completed his hours send him a reminder through Signal. Base the tone of the message on the number of reminders sent",
                )
                addInstruction(
                    "If the employee is reminded 5 times and still has still not completed the hours don't send the employee a message but ask the manager on how to respond and send the manager's response to the employee",
                )
                addInstruction(
                    "When you for sure know that the signal message is successfully sent, make sure that you update the numberOfRemindersSent for each and every the specific employee.",
                )
            }
            addToolUnit("getAllEmployeesOverview") { _: Unit ->
                getAllEmployeesOverviewHandler()
            }
            addTool("getEmployeeDetailByName") { input: EmployeeName ->
                getEmployeeByName(input)
            }
            addTool("askManagerForResponse") { input: EmployeeName ->
                getManagerResponse(input)
            }
            addTool("sendSignalMessage") { input: SignalMessage ->
                sendSignalMessage(input)
            }
            addTool("updateEmployee") { input: UpdateEmployee ->
                updateEmployee(input)
            }
            finishResponse<AgentAdministrativeResponse>()
        }.start()

    when (val result = run.result) {
        is Result.Finished -> "Hours inspected successfully: ${result.getFinishResponse<AgentAdministrativeResponse>()}"
        is Result.Stuck -> "Agent is stuck and could not complete task, it says: ${result.reason}"
        is Result.Fatal -> "Agent crashed: ${result.message}"
    }.also(::println)
}

private fun getAllEmployeesOverviewHandler(): EmployeesOverviewResponse =
    EmployeesOverviewResponse(
        """
    |Employee: Niels
    |Telephone number: 0612345678
    |
    |Employee: Henk
    |Telephone number: 0687654321
    |
    |Employee: Jan
    |Telephone number: 0643211234
        """.trimMargin(),
    )

private fun updateEmployee(input: UpdateEmployee): UpdateEmployeeResponse =
    UpdateEmployeeResponse("Updated number of reminders sent for '${input.name}' to '${input.numberOfRemindersSent}'")

private fun sendSignalMessage(input: SignalMessage): SignalMessageResponse =
    SignalMessageResponse("✉️ Sending: '${input.message}' to '${input.phoneNumber}'")

private fun getManagerResponse(input: EmployeeName): ManagerResponse =
    ManagerResponse("${input.name}, please submit your hours, you have been reminded 5 times already. Kind regards, the management")

private fun getEmployeeByName(input: EmployeeName): EmployeeDetailsResponse {
    val details =
        when (input.name) {
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
    return EmployeeDetailsResponse(details)
}

@AigenticParameter
data class EmployeeName(
    val name: String,
)

@AigenticParameter
data class UpdateEmployee(
    val name: String,
    val numberOfRemindersSent: Int,
)

@AigenticParameter
data class SignalMessage(
    val phoneNumber: String,
    val message: String,
)

@AigenticResponse
data class EmployeeDetailsResponse(val details: String)

@AigenticResponse
data class ManagerResponse(val response: String)

@AigenticResponse
data class UpdateEmployeeResponse(val message: String)

@AigenticResponse
data class SignalMessageResponse(val message: String)

@AigenticResponse
data class EmployeesOverviewResponse(val overview: String)

@AigenticParameter
data class AgentAdministrativeResponse(
    val messagedPeople: List<String>,
    val completedPeople: List<String>,
    val notCompletedPeople: List<String>,
)
