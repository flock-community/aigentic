package community.flock.aigentic.code.generation.ksp.test

import community.flock.aigentic.core.tool.Parameter
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

fun getPrimitiveParameter(
    objectParam: Parameter.Complex.Object,
    name: String,
): Parameter.Primitive {
    val param = objectParam.parameters.find { it.name == name }
    param shouldNotBe null
    param.shouldBeInstanceOf<Parameter.Primitive>()
    return param
}

fun getObjectParameter(
    objectParam: Parameter.Complex.Object,
    name: String,
): Parameter.Complex.Object {
    val param = objectParam.parameters.find { it.name == name }
    param shouldNotBe null
    param.shouldBeInstanceOf<Parameter.Complex.Object>()
    return param
}

fun getEnumParameter(
    objectParam: Parameter.Complex.Object,
    name: String,
): Parameter.Complex.Enum {
    val param = objectParam.parameters.find { it.name == name }
    param shouldNotBe null
    param.shouldBeInstanceOf<Parameter.Complex.Enum>()
    return param
}
