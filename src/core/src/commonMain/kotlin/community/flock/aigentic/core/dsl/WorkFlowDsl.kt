package community.flock.aigentic.core.dsl

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.workflow.Workflow2
import community.flock.aigentic.core.workflow.Workflow3
import community.flock.aigentic.core.workflow.Workflow4
import community.flock.aigentic.core.workflow.Workflow5

infix fun <I : Any, M : Any, O : Any> Agent<I, M>.thenProcess(next: Agent<M, O>): Workflow2<I, M, O> = Workflow2(this, next)

infix fun <I : Any, M1 : Any, M2 : Any, O : Any> Workflow2<I, M1, M2>.thenProcess(next: Agent<M2, O>): Workflow3<I, M1, M2, O> =
    Workflow3(firstAgent, secondAgent thenProcess next)

infix fun <I : Any, M1 : Any, M2 : Any, M3 : Any, O : Any> Workflow3<I, M1, M2, M3>.thenProcess(next: Agent<M3, O>): Workflow4<I, M1, M2, M3, O> =
    Workflow4(firstAgent, restWorkflow thenProcess next)

infix fun <I : Any, M1 : Any, M2 : Any, M3 : Any, M4 : Any, O : Any> Workflow4<I, M1, M2, M3, M4>.thenProcess(
    next: Agent<M4, O>,
): Workflow5<I, M1, M2, M3, M4, O> = Workflow5(firstAgent, restWorkflow thenProcess next)
