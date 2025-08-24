package community.flock.aigentic.core.workflow

import community.flock.aigentic.core.agent.Agent

data class Workflow2<I : Any, M : Any, O : Any>(
    val firstAgent: Agent<I, M>,
    val secondAgent: Agent<M, O>,
)

data class Workflow3<I : Any, M1 : Any, M2 : Any, O : Any>(
    val firstAgent: Agent<I, M1>,
    val restWorkflow: Workflow2<M1, M2, O>,
)

data class Workflow4<I : Any, M1 : Any, M2 : Any, M3 : Any, O : Any>(
    val firstAgent: Agent<I, M1>,
    val restWorkflow: Workflow3<M1, M2, M3, O>,
)

data class Workflow5<I : Any, M1 : Any, M2 : Any, M3 : Any, M4 : Any, O : Any>(
    val firstAgent: Agent<I, M1>,
    val restWorkflow: Workflow4<M1, M2, M3, M4, O>,
)
