@file:Suppress("ktlint:standard:max-line-length")

package community.flock.aigentic.example

import community.flock.aigentic.core.agent.tokenUsage
import community.flock.aigentic.core.agent.tool.Outcome
import community.flock.aigentic.core.annotations.AigenticParameter
import community.flock.aigentic.core.dsl.agent
import community.flock.aigentic.core.dsl.thenProcess
import community.flock.aigentic.core.workflow.start
import community.flock.aigentic.gemini.dsl.geminiModel
import community.flock.aigentic.gemini.model.GeminiModelIdentifier
import kotlin.time.ExperimentalTime

val employmentAgreement =
    """
    EMPLOYMENT AGREEMENT

    This Employment Agreement is entered into on March 15, 2024, between TechCorp Solutions, a Delaware corporation located at 1234 Corporate Blvd, San Francisco, CA 94102, and John Michael Smith, residing at 5678 Residential Lane, Apartment 3B, Palo Alto, CA 94301.

    Employee Information:
    - Full Name: John Michael Smith
    - Social Security Number: 123-45-6789
    - Phone: (555) 123-4567
    - Email: john.smith@email.com

    Terms of Employment:
    1. Position: Senior Software Engineer
    2. Start Date: April 1, 2024
    3. Annual Salary: $150,000
    4. Benefits: Health insurance, 401k matching, 20 days PTO
    5. Termination: Either party may terminate with 30 days written notice

    This agreement is governed by California state law. Both parties acknowledge they have read and understood all terms.

    Signatures:
    John Michael Smith - Employee
    Sarah Johnson, HR Director - TechCorp Solutions
    """.trimIndent()

@AigenticParameter
data class RedactedDocument(
    val redactedText: String,
)

@AigenticParameter
data class LegalSummary(
    val summary: String,
)

@AigenticParameter
data class ComplianceReport(
    val riskLevel: String,
    val potentialIssues: List<String>,
    val recommendations: List<String>,
)

@OptIn(ExperimentalTime::class)
suspend fun runWorkflowExample(apiKey: String) {
    val documentRedactor =
        agent<String, RedactedDocument> {
            geminiModel {
                apiKey(apiKey)
                modelIdentifier(GeminiModelIdentifier.Gemini2_5Flash)
            }
            task("Redact sensitive information from legal documents") {
                addInstruction("Replace all personal names with [REDACTED NAME]")
                addInstruction("Replace all addresses with [REDACTED ADDRESS]")
                addInstruction("Replace all phone numbers with [REDACTED PHONE]")
                addInstruction("Replace all social security numbers with [REDACTED SSN]")
                addInstruction("Replace all email addresses with [REDACTED EMAIL]")
            }
        }

    val complianceChecker =
        agent<RedactedDocument, ComplianceReport> {
            geminiModel {
                apiKey(apiKey)
                modelIdentifier(GeminiModelIdentifier.Gemini2_5Flash)
            }
            task("Analyze legal document for compliance issues and risks") {
                addInstruction("Evaluate the document for potential legal compliance issues")
                addInstruction("Assess risk level as LOW, MEDIUM, or HIGH")
                addInstruction("Identify potential issues such as missing clauses, unclear terms, or regulatory concerns")
                addInstruction("Provide actionable recommendations to address identified issues")
                addInstruction("Focus on employment law compliance, contract clarity, and regulatory requirements")
            }
        }

    val summarizer =
        agent<ComplianceReport, LegalSummary> {
            geminiModel {
                apiKey(apiKey)
                modelIdentifier(GeminiModelIdentifier.Gemini2_5Flash)
            }
            task("Create a comprehensive legal document summary including compliance findings") {
                addInstruction("Generate a summary that includes both document content and compliance analysis")
                addInstruction("Include the document type, main parties, and key terms")
                addInstruction("Incorporate the risk level and main compliance concerns")
                addInstruction("Highlight critical recommendations from the compliance analysis")
                addInstruction("Keep the summary comprehensive yet concise under 100 words")
            }
        }

    val workFlow = documentRedactor thenProcess complianceChecker thenProcess summarizer

    val workflowRun = workFlow.start(employmentAgreement)

    when (val outcome = workflowRun.outcome) {
        is Outcome.Finished -> {
            outcome.response?.let { summary ->
                println("=== LEGAL DOCUMENT SUMMARY ===")
                println(summary.summary)
                println()

                // Print token usage per agent
                println("=== TOKEN USAGE BY AGENT ===")
                workflowRun.agentRuns.forEachIndexed { index, agentRun ->
                    val agentName =
                        when (index) {
                            0 -> "Document Redactor"
                            1 -> "Compliance Checker"
                            2 -> "Summarizer"
                            else -> "Agent $index"
                        }
                    val usage = agentRun.tokenUsage()
                    println("$agentName:")
                    println("  Input tokens: ${usage.inputTokens}")
                    println("  Output tokens: ${usage.outputTokens}")
                    println("  Thinking tokens: ${usage.thinkingOutputTokens}")
                    println("  Cached tokens: ${usage.cachedInputTokens}")
                    println()
                    println("  Started at: ${agentRun.startedAt}")
                    println("  Finished at: ${agentRun.finishedAt}")
                    println()
                    println("  Messages: ${agentRun.messages.size}")
                    println("  Outcome: ${agentRun.outcome}")
                    println()
                }

                val totalUsage = workflowRun.tokenUsage()
                println("=== TOTAL WORKFLOW TOKEN USAGE ===")
                println("Total input tokens: ${totalUsage.inputTokens}")
                println("Total output tokens: ${totalUsage.outputTokens}")
                println("Total thinking tokens: ${totalUsage.thinkingOutputTokens}")
                println("Total cached tokens: ${totalUsage.cachedInputTokens}")
            }
        }
        is Outcome.Fatal -> println("Error: ${outcome.message}")
        is Outcome.Stuck -> println("Stuck: ${outcome.reason}")
    }
}
