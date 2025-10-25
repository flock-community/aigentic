@file:Suppress("ktlint:standard:max-line-length")

package community.flock.aigentic.core.workflow

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.AgentRun
import community.flock.aigentic.core.agent.Attachment
import community.flock.aigentic.core.agent.WorkflowRun
import community.flock.aigentic.core.agent.createWorkflowRun
import community.flock.aigentic.core.agent.start
import community.flock.aigentic.core.agent.tool.Outcome

@PublishedApi
internal suspend inline fun <reified I : Any, reified M : Any, reified O : Any> startWorkflowStep(
    agent: Agent<I, M>,
    input: I,
    runs: MutableList<AgentRun<*>>,
    attachments: List<Attachment>,
    continuation: suspend (M) -> AgentRun<O>,
): WorkflowRun<O> {
    val run = agent.start(input, *attachments.toTypedArray()).also(runs::add)

    return when (val outcome = run.outcome) {
        is Outcome.Finished -> {
            outcome.response?.let { intermediateResult ->
                val finalResult = continuation(intermediateResult)
                createWorkflowRun(runs, finalResult.outcome)
            } ?: run {
                val syntheticFinalRun =
                    AgentRun(
                        startedAt = run.startedAt,
                        finishedAt = run.finishedAt,
                        messages = run.messages,
                        outcome =
                            Outcome.Finished<O>(
                                description = outcome.description,
                                response = null,
                            ),
                        modelRequests = run.modelRequests,
                        configContextMessages = run.configContextMessages,
                        runAttachmentMessages = run.runAttachmentMessages,
                    )
                createWorkflowRun(runs, syntheticFinalRun.outcome)
            }
        }

        is Outcome.Stuck -> createWorkflowRun(runs, outcome)
        is Outcome.Fatal -> createWorkflowRun(runs, outcome)
    }
}

@PublishedApi
internal suspend inline fun <reified I : Any, reified M : Any, reified O : Any> executeWorkflow(
    firstAgent: Agent<I, M>,
    input: I,
    attachments: List<Attachment>,
    continuation: suspend (M, MutableList<AgentRun<*>>) -> AgentRun<O>,
): WorkflowRun<O> {
    val agentRuns = mutableListOf<AgentRun<*>>()
    return startWorkflowStep(firstAgent, input, agentRuns, attachments) { intermediateResult ->
        continuation(intermediateResult, agentRuns)
    }
}

suspend inline fun <reified I : Any, reified M : Any, reified O : Any> Workflow2<I, M, O>.start(
    input: I,
    vararg attachments: Attachment,
): WorkflowRun<O> =
    executeWorkflow(firstAgent, input, attachments.toList()) { intermediateResult, agentRuns ->
        secondAgent.start(intermediateResult).also(agentRuns::add)
    }

suspend inline fun <reified I : Any, reified M1 : Any, reified M2 : Any, reified O : Any> Workflow3<I, M1, M2, O>.start(
    input: I,
    vararg attachments: Attachment,
): WorkflowRun<O> =
    executeWorkflow(firstAgent, input, attachments.toList()) { intermediateResult, agentRuns ->
        restWorkflow.start(intermediateResult)
            .also { agentRuns.addAll(it.agentRuns) }
            .agentRuns.last() as AgentRun<O>
    }

suspend inline fun <reified I : Any, reified M1 : Any, reified M2 : Any, reified M3 : Any, reified O : Any> Workflow4<I, M1, M2, M3, O>.start(
    input: I,
    vararg attachments: Attachment,
): WorkflowRun<O> =
    executeWorkflow(firstAgent, input, attachments.toList()) { intermediateResult, agentRuns ->
        restWorkflow.start(intermediateResult)
            .also { agentRuns.addAll(it.agentRuns) }
            .agentRuns.last() as AgentRun<O>
    }

suspend inline fun <reified I : Any, reified M1 : Any, reified M2 : Any, reified M3 : Any, reified M4 : Any, reified O : Any> Workflow5<I, M1, M2, M3, M4, O>.start(
    input: I,
    vararg attachments: Attachment,
): WorkflowRun<O> =
    executeWorkflow(firstAgent, input, attachments.toList()) { intermediateResult, agentRuns ->
        restWorkflow.start(intermediateResult)
            .also { agentRuns.addAll(it.agentRuns) }
            .agentRuns.last() as AgentRun<O>
    }
