@file:Suppress("ktlint:standard:max-line-length")

package community.flock.aigentic.cloud.google.function.http

import community.flock.aigentic.cloud.google.function.http.dsl.Authentication.AuthorizationHeader
import community.flock.aigentic.cloud.google.function.http.dsl.GoogleHttpCloudFunction
import community.flock.aigentic.cloud.google.function.http.dsl.HttpCloudFunctionConfig
import community.flock.aigentic.cloud.google.function.util.createRequestResponse
import community.flock.aigentic.cloud.google.function.util.finishedTask
import community.flock.aigentic.cloud.google.function.util.imStuck
import community.flock.aigentic.cloud.google.function.util.modelFinishDirectly
import community.flock.aigentic.cloud.google.function.util.testTool
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class HttpRequestHandlerTest : DescribeSpec({

    val finishedTaskConfig: HttpCloudFunctionConfig.() -> Unit = {
        authentication(AuthorizationHeader("some-secret-key"))
        agent {
            model(modelFinishDirectly(finishedTask))
            task("Respond with a welcome message to the person") {
                addInstruction(
                    "Call the finishedOrStuck tool when finished and use only the message received from getCloudMessage as description, use no other text",
                )
            }
            addTool(testTool)
        }
    }

    val imStuckConfig: HttpCloudFunctionConfig.() -> Unit = {
        authentication(AuthorizationHeader("some-secret-key"))
        agent {
            model(modelFinishDirectly(imStuck))
            task("Respond with a welcome message to the person") {
                addInstruction(
                    "Call the finishedOrStuck tool when finished and use only the message received from getCloudMessage as description, use no other text",
                )
            }
            addTool(testTool)
        }
    }

    it("should throw exception when authorization header is not correct") {

        val httpCloudFunction = finishedTaskConfig.build()
        val (googleRequest, googleResponseWrapper) = createRequestResponse(true)

        shouldThrow<Exception> {
            httpCloudFunction.handleRequest(googleRequest, googleResponseWrapper.googleResponse)
        }

        with(googleResponseWrapper) {
            statusCode shouldBe 401
            response shouldBe "Unauthorized"
        }
    }

    it("should return 200 when agent is successful") {

        val httpCloudFunction = finishedTaskConfig.build()
        val (googleRequest, googleResponseWrapper) = createRequestResponse()

        httpCloudFunction.handleRequest(googleRequest, googleResponseWrapper.googleResponse)

        with(googleResponseWrapper) {
            statusCode shouldBe 200
            response shouldBe "Finished the task"
        }
    }

    it("should return 422 when agent couldn't complete task") {

        val httpCloudFunction = imStuckConfig.build()
        val (googleRequest, googleResponseWrapper) = createRequestResponse()

        httpCloudFunction.handleRequest(googleRequest, googleResponseWrapper.googleResponse)

        with(googleResponseWrapper) {
            statusCode shouldBe 422
            response shouldBe "I couldn't finish the task"
        }
    }
})

fun (HttpCloudFunctionConfig.() -> Unit).build(): GoogleHttpCloudFunction {
    return HttpCloudFunctionConfig().apply(this).build()
}