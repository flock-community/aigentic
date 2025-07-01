@file:Suppress("ktlint:standard:max-line-length")

package community.flock.aigentic.cloud.google.function.http

import community.flock.aigentic.cloud.google.function.http.dsl.Authentication.AuthorizationHeader
import community.flock.aigentic.cloud.google.function.http.dsl.GoogleHttpCloudFunction
import community.flock.aigentic.cloud.google.function.http.dsl.HttpCloudFunctionConfig
import community.flock.aigentic.cloud.google.function.util.createRequestResponse
import community.flock.aigentic.cloud.google.function.util.finishedTaskToolCall
import community.flock.aigentic.cloud.google.function.util.finishedTaskWithResponseToolCall
import community.flock.aigentic.cloud.google.function.util.modelException
import community.flock.aigentic.cloud.google.function.util.modelFinishDirectly
import community.flock.aigentic.cloud.google.function.util.stuckWithTaskToolCall
import community.flock.aigentic.cloud.google.function.util.testTool
import community.flock.aigentic.core.tool.Parameter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class HttpRequestHandlerTest : DescribeSpec({

    val finishedTaskConfig: HttpCloudFunctionConfig<Unit, Unit>.() -> Unit = {
        authentication(AuthorizationHeader("some-secret-key"))
        agent {
            model(modelFinishDirectly(finishedTaskToolCall))
            task("Respond with a welcome message to the person") {}
            addTool(testTool)
        }
    }

    val finishedTaskWithResponseConfig: HttpCloudFunctionConfig<Unit, JsonObject>.() -> Unit = {
        authentication(AuthorizationHeader("some-secret-key"))
        agent {
            model(modelFinishDirectly(finishedTaskWithResponseToolCall))
            task("Respond with a welcome message to the person") {}
            addTool(testTool)
            finishResponse(
                Parameter.Complex.Object(
                    name = "response",
                    description = "some description",
                    true,
                    parameters = emptyList(),
                ),
            )
        }
    }

    val imStuckConfig: HttpCloudFunctionConfig<Unit, Unit>.() -> Unit = {
        authentication(AuthorizationHeader("some-secret-key"))
        agent {
            model(modelFinishDirectly(stuckWithTaskToolCall))
            task("Respond with a welcome message to the person") {}
            addTool(testTool)
        }
    }

    val fatalConfig: HttpCloudFunctionConfig<Unit, Unit>.() -> Unit = {
        authentication(AuthorizationHeader("some-secret-key"))
        agent {
            model(modelException())
            task("Respond with a welcome message to the person") {}
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

    it("should return 200 when agent is successful and have a response if finishedResponse is set") {

        val httpCloudFunction = finishedTaskWithResponseConfig.build()
        val (googleRequest, googleResponseWrapper) = createRequestResponse()

        httpCloudFunction.handleRequest(googleRequest, googleResponseWrapper.googleResponse)

        with(googleResponseWrapper) {
            statusCode shouldBe 200
            (response as JsonObject)["message"] shouldBe JsonPrimitive("Agent response")
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

    it("should return 500 when agent encountered an exception") {

        val httpCloudFunction = fatalConfig.build()
        val (googleRequest, googleResponseWrapper) = createRequestResponse()

        httpCloudFunction.handleRequest(googleRequest, googleResponseWrapper.googleResponse)

        with(googleResponseWrapper) {
            statusCode shouldBe 500
            response shouldBe "Internal Server Error"
        }
    }

    it("should intercept request and pass intercepted request to agent") {

        val interceptedRequestConfig: HttpCloudFunctionConfig<Unit, Unit>.() -> Unit = {
            authentication(AuthorizationHeader("some-secret-key"))
            requestInterceptor { request ->
                request.copy(
                    headers = mapOf("intercepted" to "true"),
                )
            }
            agent {
                it.headers shouldBe mapOf("intercepted" to "true")
                model(modelFinishDirectly(finishedTaskToolCall))
                task("Respond with a welcome message to the person") {}
                addTool(testTool)
            }
        }

        val httpCloudFunction = interceptedRequestConfig.build()
        val (googleRequest, googleResponseWrapper) = createRequestResponse()

        httpCloudFunction.handleRequest(googleRequest, googleResponseWrapper.googleResponse)

        with(googleResponseWrapper) {
            statusCode shouldBe 200
        }
    }

    it("should return 500 when exception is thrown in requestInterceptor") {

        val interceptedRequestConfig: HttpCloudFunctionConfig<Unit, Unit>.() -> Unit = {
            authentication(AuthorizationHeader("some-secret-key"))
            requestInterceptor { request ->
                error("Request interceptor error")
            }
            agent {
                it.headers shouldBe mapOf("intercepted" to "true")
                model(modelFinishDirectly(finishedTaskToolCall))
                task("Respond with a welcome message to the person") {}
                addTool(testTool)
            }
        }

        val httpCloudFunction = interceptedRequestConfig.build()
        val (googleRequest, googleResponseWrapper) = createRequestResponse()

        shouldThrow<Exception> {
            httpCloudFunction.handleRequest(googleRequest, googleResponseWrapper.googleResponse)
        }

        with(googleResponseWrapper) {
            statusCode shouldBe 500
        }
    }
})

inline fun <reified I : Any, O : Any> (HttpCloudFunctionConfig<I, O>.() -> Unit).build(): GoogleHttpCloudFunction<I, O> {
    return HttpCloudFunctionConfig<I, O>().apply(this).build()
}
