package community.flock.aigentic.tools.openapi

import community.flock.aigentic.core.exception.aigenticException
import community.flock.aigentic.core.logging.Logger
import community.flock.aigentic.core.logging.SimpleLogger
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.ParameterType.Primitive
import community.flock.aigentic.core.tool.PrimitiveValue
import community.flock.aigentic.tools.http.EndpointOperation
import community.flock.aigentic.tools.http.EndpointOperation.Method.DELETE
import community.flock.aigentic.tools.http.EndpointOperation.Method.GET
import community.flock.aigentic.tools.http.EndpointOperation.Method.PATCH
import community.flock.aigentic.tools.http.EndpointOperation.Method.POST
import community.flock.aigentic.tools.http.EndpointOperation.Method.PUT
import community.flock.kotlinx.openapi.bindings.v3.MediaType
import community.flock.kotlinx.openapi.bindings.v3.OpenAPI
import community.flock.kotlinx.openapi.bindings.v3.OpenAPIObject
import community.flock.kotlinx.openapi.bindings.v3.OperationObject
import community.flock.kotlinx.openapi.bindings.v3.ParameterLocation
import community.flock.kotlinx.openapi.bindings.v3.ParameterObject
import community.flock.kotlinx.openapi.bindings.v3.ParameterOrReferenceObject
import community.flock.kotlinx.openapi.bindings.v3.Path
import community.flock.kotlinx.openapi.bindings.v3.ReferenceObject
import community.flock.kotlinx.openapi.bindings.v3.RequestBodyObject
import community.flock.kotlinx.openapi.bindings.v3.RequestBodyOrReferenceObject
import community.flock.kotlinx.openapi.bindings.v3.SchemaObject
import community.flock.kotlinx.openapi.bindings.v3.SchemaOrReferenceObject
import community.flock.kotlinx.openapi.bindings.v3.Type
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

class OpenAPIv3Parser(
    private val openApi: OpenAPIObject,
    private val logger: Logger,
) {
    companion object {
        fun parseOperations(
            json: String,
            logger: Logger = SimpleLogger,
        ): List<EndpointOperation> =
            OpenAPI(json = Json { ignoreUnknownKeys = true }).decodeFromString(json).let {
                OpenAPIv3Parser(it, logger).getEndpointOperations()
            }
    }

    fun getEndpointOperations(): List<EndpointOperation> =
        openApi.paths.flatMap { (path, pathItemObject) ->
            listOfNotNull(
                pathItemObject.delete?.toEndpointOperation(DELETE, path),
                pathItemObject.get?.toEndpointOperation(GET, path),
                pathItemObject.post?.toEndpointOperation(POST, path),
                pathItemObject.put?.toEndpointOperation(PUT, path),
                pathItemObject.patch?.toEndpointOperation(PATCH, path),
            )
        }

    private fun OperationObject.toEndpointOperation(
        method: EndpointOperation.Method,
        path: Path,
    ): EndpointOperation {
        val parameters = getParameters()
        return EndpointOperation(
            name = operationId ?: "$method ${path.value}",
            description = description,
            method = method,
            url = getEndpointUrl(path),
            pathParams = parameters.path,
            queryParams = parameters.query,
            requestBody = getRequestBody(),
        )
    }

    private fun getEndpointUrl(path: Path): String {
        return if (openApi.servers?.size != 1) {
            aigenticException("Expecting exactly 1 server in OAS, got ${openApi.servers}")
        } else {
            val firstUrl = openApi.servers?.first()?.url ?: aigenticException("OAS server url required")
            firstUrl.trimEnd('/') + "/" + path.value.trimStart('/')
        }
    }

    private fun OperationObject.getParameters(): Parameters =
        parameters?.fold(Parameters()) { parameters, parameterOrReferenceObject ->
            parameterOrReferenceObject.resolve().let { parameterObject: ParameterObject ->
                val schemaObject = parameterObject.schema?.resolve() ?: aigenticException("Schema cannot be null")

                val parameter =
                    schemaObject.toParameter(
                        name = parameterObject.name,
                        description = parameterObject.description,
                        isRequired = parameterObject.required ?: false,
                    )

                when (parameterObject.`in`) {
                    ParameterLocation.QUERY -> parameters.copy(query = parameters.query + listOfNotNull(parameter))
                    ParameterLocation.PATH -> parameters.copy(path = parameters.path + listOfNotNull(parameter))
                    ParameterLocation.COOKIE, ParameterLocation.HEADER -> parameters // Ignored
                }
            }
        } ?: Parameters()

    private fun OperationObject.getRequestBody(): Parameter.Complex.Object? =
        requestBody?.resolve()?.let { requestBodyObject ->
            requestBodyObject.content?.get(MediaType("application/json"))?.schema?.resolve()?.let {
                it.createObjectParameter(
                    name = it.xml?.name ?: "body",
                    description = requestBodyObject.description,
                    isRequired = requestBodyObject.required == true,
                )
            }
        }

    private fun Map<String, SchemaOrReferenceObject>.getParameters(required: List<String>?): List<Parameter> =
        mapValues { it.value.resolve() }.mapNotNull { (name, schemaObject) ->
            fun isRequired(name: String) = required?.contains(name) ?: false
            schemaObject.toParameter(name = name, description = schemaObject.description, isRequired = isRequired(name))
        }

    private fun SchemaObject.toParameter(
        name: String,
        description: String?,
        isRequired: Boolean,
    ): Parameter? {
        return when (val type = determineType()) {
            null -> null
            ParameterType.Complex.Array ->
                createArrayParameter(
                    name,
                    description,
                    isRequired,
                )

            ParameterType.Complex.Enum -> createEnumParameter(name, description, isRequired)
            ParameterType.Complex.Object ->
                createObjectParameter(
                    name,
                    description,
                    isRequired,
                )

            is Primitive -> createPrimitiveParameter(name, description, isRequired, type)
        }
    }

    private fun SchemaObject.createEnumParameter(
        name: String,
        description: String?,
        isRequired: Boolean,
    ): Parameter.Complex.Enum {
        val enumValueType = determineEnumValueType()
        val enumValues = createEnumValues(enumValueType)

        return Parameter.Complex.Enum(
            name = name,
            description = description,
            isRequired = isRequired,
            default = enumValues.firstOrNull { it.value.toString() == default?.jsonPrimitive?.content },
            values = enumValues,
            valueType = enumValueType,
        )
    }

    private fun SchemaObject.determineEnumValueType(): Primitive =
        when (type) {
            null -> aigenticException("Enum value type cannot be null")
            Type.STRING -> Primitive.String
            Type.NUMBER -> Primitive.Number
            Type.INTEGER -> Primitive.Integer
            Type.BOOLEAN -> Primitive.Boolean // Not sure why you want to use a boolean in an enum.... but it's possible
            Type.OBJECT, Type.ARRAY -> aigenticException("Only primitive values are supported for enum, got: $type")
        }

    private fun SchemaObject.createEnumValues(parameterType: Primitive) =
        when (parameterType) {
            Primitive.Boolean -> PrimitiveValue.Boolean::fromString
            Primitive.Integer -> PrimitiveValue.Integer::fromString
            Primitive.Number -> PrimitiveValue.Number::fromString
            Primitive.String -> PrimitiveValue.String::fromString
        }.let { constructor ->
            enum?.map { constructor(it.content) } ?: aigenticException("Enum values cannot be null")
        }

    private fun SchemaObject.createArrayParameter(
        name: String,
        description: String?,
        isRequired: Boolean,
    ): Parameter.Complex.Array? =
        createArrayItemParameterDefinition(
            arrayItemSchemaObject = items?.resolve() ?: aigenticException("Array items cannot be null"),
            isRequired = isRequired,
        )?.let {
            Parameter.Complex.Array(
                name = name,
                description = description,
                isRequired = isRequired,
                itemDefinition = it,
            )
        }

    private fun createArrayItemParameterDefinition(
        arrayItemSchemaObject: SchemaObject,
        isRequired: Boolean,
    ): Parameter? {
        val arrayItemSchemaParameterType = arrayItemSchemaObject.determineType()
        val name = arrayItemSchemaObject.xml?.name ?: "item"
        val description = arrayItemSchemaObject.description

        // Is this duplication with the top level?
        return when (arrayItemSchemaParameterType) {
            null -> null
            ParameterType.Complex.Array -> arrayItemSchemaObject.createArrayParameter("item", null, false)

            ParameterType.Complex.Object ->
                Parameter.Complex.Object(
                    name = name,
                    description = description,
                    isRequired = isRequired,
                    parameters =
                        arrayItemSchemaObject.properties?.getParameters(
                            arrayItemSchemaObject.required,
                        ) ?: aigenticException("Object properties cannot be empty for parameter: $name ($description)"),
                )

            is Primitive ->
                createPrimitiveParameter(
                    name = name,
                    description = description,
                    isRequired = isRequired,
                    type = arrayItemSchemaParameterType,
                )

            ParameterType.Complex.Enum ->
                arrayItemSchemaObject.createEnumParameter(
                    name = name,
                    description = description,
                    isRequired = isRequired,
                )
        }
    }

    private fun createPrimitiveParameter(
        name: String,
        description: String?,
        isRequired: Boolean,
        type: Primitive,
    ) = Parameter.Primitive(
        name = name,
        description = description,
        isRequired = isRequired,
        type = type,
    )

    private fun SchemaObject.createObjectParameter(
        name: String,
        description: String?,
        isRequired: Boolean,
    ): Parameter.Complex.Object =
        Parameter.Complex.Object(
            name = name,
            description = description,
            isRequired = isRequired,
            parameters = properties?.getParameters(this.required) ?: emptyList(),
        )

    private fun SchemaObject.determineType(): ParameterType? {
        val isUnion = oneOf != null || anyOf != null || allOf != null
        return when (val schemaObjectType = this.type) {
            null -> {
                when {
                    isUnion ->
                        null.also {
                            logger.warning("No type found and union types (oneOf, anyOf, allOf) not yet supported")
                        }

                    else ->
                        null.also {
                            logger.warning("Cannot determine type for schema: $this")
                        }
                }
            }

            else -> {
                val type = schemaObjectType.toParameterType()
                val isEnum = enum?.isNotEmpty() ?: false

                when {
                    type is Primitive && !isEnum -> type
                    type is Primitive && isEnum -> ParameterType.Complex.Enum
                    type is ParameterType.Complex.Array -> ParameterType.Complex.Array
                    type is ParameterType.Complex.Object && !isUnion -> ParameterType.Complex.Object
                    type is ParameterType.Complex.Object && isUnion ->
                        null.also {
                            logger.warning("Type found: '$type' but union types (oneOf, anyOf, allOf) not yet supported")
                        }

                    else ->
                        null.also {
                            logger.warning("Got type: '$type' but unable to determine parameter type for schema: $this")
                        }
                }
            }
        }
    }

    private fun Type.toParameterType(): ParameterType =
        when (this) {
            Type.STRING -> Primitive.String
            Type.NUMBER -> Primitive.Number
            Type.INTEGER -> Primitive.Integer
            Type.BOOLEAN -> Primitive.Boolean
            Type.ARRAY -> ParameterType.Complex.Array
            Type.OBJECT -> ParameterType.Complex.Object
        }

    private fun ReferenceObject.getReference() = this.ref.value.split("/").getOrNull(3) ?: aigenticException("Wrong reference: ${this.ref.value}")

    private fun ParameterOrReferenceObject.resolve(): ParameterObject {
        fun ReferenceObject.resolveParameterObject(): ParameterObject =
            openApi.components?.parameters?.get(getReference())?.let {
                when (it) {
                    is ParameterObject -> it
                    is ReferenceObject -> it.resolveParameterObject()
                }
            } ?: aigenticException("Cannot resolve $ref")

        return when (this) {
            is ParameterObject -> this
            is ReferenceObject -> this.resolveParameterObject()
        }
    }

    private fun SchemaOrReferenceObject.resolve(): SchemaObject {
        fun ReferenceObject.resolveSchemaObject(): SchemaObject =
            openApi.components?.schemas?.get(getReference())?.let {
                when (it) {
                    is SchemaObject -> it
                    is ReferenceObject -> it.resolveSchemaObject()
                }
            } ?: aigenticException("Cannot resolve ref: $ref")

        return when (this) {
            is ReferenceObject -> this.resolveSchemaObject()
            is SchemaObject -> this
        }
    }

    private fun RequestBodyOrReferenceObject.resolve(): RequestBodyObject {
        fun ReferenceObject.resolveRequestBody(): RequestBodyObject =
            openApi.components?.requestBodies?.get(getReference())?.let {
                when (it) {
                    is RequestBodyObject -> it
                    is ReferenceObject -> it.resolveRequestBody()
                }
            } ?: aigenticException("Cannot resolve ref: $ref")

        return when (this) {
            is RequestBodyObject -> this
            is ReferenceObject -> this.resolveRequestBody()
        }
    }
}

data class Parameters(
    val query: List<Parameter> = listOf(),
    val path: List<Parameter> = listOf(),
)
