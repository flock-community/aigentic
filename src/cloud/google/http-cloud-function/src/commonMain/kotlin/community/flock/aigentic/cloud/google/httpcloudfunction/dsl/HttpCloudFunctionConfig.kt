//package community.flock.aigentic.cloud.google.httpcloudfunction.dsl
//
//import community.flock.aigentic.core.agent.Agent
//import community.flock.aigentic.core.dsl.AgentDSL
//import community.flock.aigentic.core.dsl.Config
//
//fun googleHttpCloudFunction(config: HttpCloudFunctionConfig.() -> Unit) = HttpCloudFunctionConfig().apply(config).build()
//
//@AgentDSL
//class HttpCloudFunctionConfig : Config<Unit> {
//    internal var agent: Agent? = null
//
//    fun agent(agent: Agent) {
//        this.agent = agent
//    }
//
//    override fun build() {
//    }
//}
