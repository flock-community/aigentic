package community.flock.aigentic.tools.http

import community.flock.aigentic.core.tool.Parameter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmInline

@JvmInline
value class ResolvedQueryParameters(val values: Map<String, JsonElement>) {

    companion object {
        fun empty() = ResolvedQueryParameters(emptyMap())
    }
}

@JvmInline
value class ResolvedRequestBody(val stringBody: String)

@JvmInline
value class ResolvedUrl(val urlString: String)


interface QueryParametersArgumentsResolver {

    fun resolveQueryParameters(
        queryParameters: List<Parameter>, callArguments: JsonObject
    ): ResolvedQueryParameters
}

class DefaultQueryParametersArgumentsResolver : QueryParametersArgumentsResolver {

    override fun resolveQueryParameters(
        queryParameters: List<Parameter>, callArguments: JsonObject
    ): ResolvedQueryParameters = queryParameters.mapNotNull { queryParameter ->

        val parameterValue = queryParameter.getParameterValue(callArguments)
        parameterValue?.let {
            queryParameter.name to it
        }

    }.toMap().let {
        ResolvedQueryParameters(it)
    }
}


interface RequestBodyArgumentsResolver {
    fun resolveRequestBody(
        requestBodyParameter: Parameter.Complex.Object, callArguments: JsonObject
    ): ResolvedRequestBody?
}

class DefaultRequestBodyArgumentsResolver : RequestBodyArgumentsResolver {

    override fun resolveRequestBody(
        requestBodyParameter: Parameter.Complex.Object, callArguments: JsonObject
    ): ResolvedRequestBody? {
        val parameterValue = requestBodyParameter.getParameterValue(callArguments)
        return parameterValue?.let { ResolvedRequestBody(Json.encodeToString(it)) }
    }
}

interface UrlArgumentsResolver {
    fun resolveUrl(
        placeHolderUrl: String, pathParameters: List<Parameter>, callArguments: JsonObject
    ): ResolvedUrl
}

class DefaultUrlArgumentsResolver : UrlArgumentsResolver {
    override fun resolveUrl(
        placeHolderUrl: String, pathParameters: List<Parameter>, callArguments: JsonObject
    ): ResolvedUrl {

        // TODO Url encoding?
        return pathParameters.fold(placeHolderUrl) { url, pathParam ->
            val paramValue = pathParam.getParameterValue(callArguments)
            if (paramValue == null) {
                error("Path parameter '${pathParam.name}' required for url '$url' but not found in call arguments.")
            } else {
                url.replace("{${pathParam.name}}", paramValue.jsonPrimitive.content)
            }
        }.let {
            if (it.contains("{") || it.contains("}")) {
                error("Not all path parameters are resolved: '$it'")
            } else {
                ResolvedUrl(it)
            }
        }
    }
}

fun Parameter.getParameterValue(arguments: JsonObject): JsonElement? {
    val paramValue = arguments[name]
    return when {
        paramValue == null && isRequired -> error("Param $name is required but is not present in call arguments! $arguments")
        paramValue != null -> paramValue
        else -> null
    }
}
