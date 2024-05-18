//package community.flock.aigentic.cloud.google.httpcloudfunction
//
//import community.flock.aigentic.cloud.google.httpcloudfunction.dsl.googleHttpCloudFunction
//import community.flock.aigentic.core.dsl.agent
//import io.kotest.core.spec.style.DescribeSpec
//
//class HttpCloudFunctionTest : DescribeSpec({
//
//    describe("HttpCloudFunctionTest") {
//
//        it("should pass") {
//
//            googleHttpCloudFunction {
//                agent =
//                    agent {
//                        task("Summarize the retrieved news events") {
//                            addInstruction("Fetch top 10 news events")
//                            addInstruction("Summarize the results")
//                        }
//                    }
//            }
//        }
//    }
//})
