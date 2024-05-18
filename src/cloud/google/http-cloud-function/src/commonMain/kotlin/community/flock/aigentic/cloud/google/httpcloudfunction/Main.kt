@file:OptIn(ExperimentalEncodingApi::class, ExperimentalJsExport::class)

package community.flock.aigentic.cloud.google.httpcloudfunction

import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.start
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.io.encoding.ExperimentalEncodingApi

object RunConfig {
    var agent: Agent? = null
}

@JsExport
fun main() {
    functions.http("helloGET") { request, response ->

        if(RunConfig.agent == null) {
            response.status(503).send("No agent to run")
        }

        println("Request received: $request")
        CoroutineScope(Dispatchers.Default).launch {
            val run = RunConfig.agent?.start()
            println("Agent finished: $run")
            response.send(run)
        }


        response.send("Hello from GETTT!")
    }

}
