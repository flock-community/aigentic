package community.flock.aigentic.core.agent

data class Expected<O : Any>(
    val evaluationSet: String,
    val output: O,
)
