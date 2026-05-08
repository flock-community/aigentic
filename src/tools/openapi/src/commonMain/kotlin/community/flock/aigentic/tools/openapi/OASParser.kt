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
import community.flock.kotlinx.openapi.bindings.MediaType
import community.flock.kotlinx.openapi.bindings.OpenAPIV3
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Model
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Operation
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Parameter
import community.flock.kotlinx.openapi.bindings.OpenAPIV3ParameterLocation
import community.flock.kotlinx.openapi.bindings.OpenAPIV3ParameterOrReference
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Reference
import community.flock.kotlinx.openapi.bindings.OpenAPIV3RequestBody
import community.flock.kotlinx.openapi.bindings.OpenAPIV3RequestBodyOrReference
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV3SchemaOrReference
import community.flock.kotlinx.openapi.bindings.OpenAPIV3SingleType
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Type
import community.flock.kotlinx.openapi.bindings.OpenAPIV3TypeArray
import community.flock.kotlinx.openapi.bindings.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

class OpenAPIv3Parser(
    private val openApi: OpenAPIV3Model,
    private val logger: Logger,
) {
    companion object {
        fun parseOperations(
            json: String,
            logger: Logger = SimpleLogger,
        ): List<EndpointOperation> =
            OpenAPIV3(json = Json { ignoreUnknownKeys = true }).decodeFromString(json).let {
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

    private fun OpenAPIV3Operation.toEndpointOperation(
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

    private fun getEndpointUrl(path: Path): String =
        if (openApi.servers?.size != 1) {
            aigenticException("Expecting exactly 1 server in OAS, got ${openApi.servers}")
        } else {
            val firstUrl = openApi.servers?.first()?.url ?: aigenticException("OAS server url required")
            firstUrl.trimEnd('/') + "/" + path.value.trimStart('/')
        }

    private fun OpenAPIV3Operation.getParameters(): Parameters =
        parameters?.fold(Parameters()) { parameters, parameterOrReferenceObject ->
            parameterOrReferenceObject.resolve().let { parameterObject: OpenAPIV3Parameter ->
                val schemaObject = parameterObject.schema?.resolve() ?: aigenticException("Schema cannot be null")

                val parameter =
                    schemaObject.toParameter(
                        name = parameterObject.name,
                        description = parameterObject.description,
                        isRequired = parameterObject.required ?: false,
                    )

                when (parameterObject.`in`) {
                    OpenAPIV3ParameterLocation.QUERY -> parameters.copy(query = parameters.query + listOfNotNull(parameter))
                    OpenAPIV3ParameterLocation.PATH -> parameters.copy(path = parameters.path + listOfNotNull(parameter))
                    OpenAPIV3ParameterLocation.COOKIE, OpenAPIV3ParameterLocation.HEADER -> parameters // Ignored
                }
            }
        } ?: Parameters()

    private fun OpenAPIV3Operation.getRequestBody(): Parameter.Complex.Object? =
        requestBody?.resolve()?.let { requestBodyObject ->
            requestBodyObject.content?.get(MediaType("application/json"))?.schema?.resolve()?.let {
                it.createObjectParameter(
                    name = it.xml?.name ?: "body",
                    description = requestBodyObject.description,
                    isRequired = requestBodyObject.required == true,
                )
            }
        }

    private fun Map<String, OpenAPIV3SchemaOrReference>.getParameters(required: List<String>?): List<Parameter> =
        mapValues { it.value.resolve() }.mapNotNull { (name, schemaObject) ->
            fun isRequired(name: String) = required?.contains(name) ?: false
            schemaObject.toParameter(name = name, description = schemaObject.description, isRequired = isRequired(name))
        }

    private fun OpenAPIV3Schema.toParameter(
        name: String,
        description: String?,
        isRequired: Boolean,
    ): Parameter? =
        when (val type = determineType()) {
            null -> {
                null
            }

            ParameterType.Complex.Array -> {
                createArrayParameter(
                    name,
                    description,
                    isRequired,
                )
            }

            ParameterType.Complex.Enum -> {
                createEnumParameter(name, description, isRequired)
            }

            ParameterType.Complex.Object -> {
                createObjectParameter(
                    name,
                    description,
                    isRequired,
                )
            }

            is Primitive -> {
                createPrimitiveParameter(name, description, isRequired, type)
            }
        }

    private fun OpenAPIV3Schema.createEnumParameter(
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

    private fun OpenAPIV3Schema.determineEnumValueType(): Primitive =
        when (type.toSingleType()) {
            null -> {
                aigenticException("Enum value type cannot be null")
            }

            OpenAPIV3Type.STRING -> {
                Primitive.String
            }

            OpenAPIV3Type.NUMBER -> {
                Primitive.Number
            }

            OpenAPIV3Type.INTEGER -> {
                Primitive.Integer
            }

            OpenAPIV3Type.BOOLEAN -> {
                Primitive.Boolean
            }

            // Not sure why you want to use a boolean in an enum.... but it's possible
            OpenAPIV3Type.OBJECT, OpenAPIV3Type.ARRAY, OpenAPIV3Type.NULL -> {
                aigenticException("Only primitive values are supported for enum, got: ${type.toSingleType()}")
            }
        }

    private fun OpenAPIV3Schema.createEnumValues(parameterType: Primitive) =
        when (parameterType) {
            Primitive.Boolean -> PrimitiveValue.Boolean::fromString
            Primitive.Integer -> PrimitiveValue.Integer::fromString
            Primitive.Number -> PrimitiveValue.Number::fromString
            Primitive.String -> PrimitiveValue.String::fromString
        }.let { constructor ->
            enum?.map { constructor(it.content) } ?: aigenticException("Enum values cannot be null")
        }

    private fun OpenAPIV3Schema.createArrayParameter(
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
        arrayItemSchemaObject: OpenAPIV3Schema,
        isRequired: Boolean,
    ): Parameter? {
        val arrayItemSchemaParameterType = arrayItemSchemaObject.determineType()
        val name = arrayItemSchemaObject.xml?.name ?: "item"
        val description = arrayItemSchemaObject.description

        // Is this duplication with the top level?
        return when (arrayItemSchemaParameterType) {
            null -> {
                null
            }

            ParameterType.Complex.Array -> {
                arrayItemSchemaObject.createArrayParameter("item", null, false)
            }

            ParameterType.Complex.Object -> {
                Parameter.Complex.Object(
                    name = name,
                    description = description,
                    isRequired = isRequired,
                    parameters =
                        arrayItemSchemaObject.properties?.getParameters(
                            arrayItemSchemaObject.required,
                        ) ?: aigenticException("Object properties cannot be empty for parameter: $name ($description)"),
                )
            }

            is Primitive -> {
                createPrimitiveParameter(
                    name = name,
                    description = description,
                    isRequired = isRequired,
                    type = arrayItemSchemaParameterType,
                )
            }

            ParameterType.Complex.Enum -> {
                arrayItemSchemaObject.createEnumParameter(
                    name = name,
                    description = description,
                    isRequired = isRequired,
                )
            }
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

    private fun OpenAPIV3Schema.createObjectParameter(
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

    private fun OpenAPIV3Schema.determineType(): ParameterType? {
        val isUnion = oneOf != null || anyOf != null || allOf != null
        return when (val schemaObjectType = this.type.toSingleType()) {
            null -> {
                when {
                    isUnion -> {
                        null.also {
                            logger.warning("No type found and union types (oneOf, anyOf, allOf) not yet supported")
                        }
                    }

                    else -> {
                        null.also {
                            logger.warning("Cannot determine type for schema: $this")
                        }
                    }
                }
            }

            else -> {
                val type = schemaObjectType.toParameterType()
                val isEnum = enum?.isNotEmpty() ?: false

                when {
                    type is Primitive && !isEnum -> {
                        type
                    }

                    type is Primitive && isEnum -> {
                        ParameterType.Complex.Enum
                    }

                    type is ParameterType.Complex.Array -> {
                        ParameterType.Complex.Array
                    }

                    type is ParameterType.Complex.Object && !isUnion -> {
                        ParameterType.Complex.Object
                    }

                    type is ParameterType.Complex.Object && isUnion -> {
                        null.also {
                            logger.warning("Type found: '$type' but union types (oneOf, anyOf, allOf) not yet supported")
                        }
                    }

                    else -> {
                        null.also {
                            logger.warning("Got type: '$type' but unable to determine parameter type for schema: $this")
                        }
                    }
                }
            }
        }
    }

    private fun OpenAPIV3Type.toParameterType(): ParameterType? =
        when (this) {
            OpenAPIV3Type.STRING -> Primitive.String
            OpenAPIV3Type.NUMBER -> Primitive.Number
            OpenAPIV3Type.INTEGER -> Primitive.Integer
            OpenAPIV3Type.BOOLEAN -> Primitive.Boolean
            OpenAPIV3Type.ARRAY -> ParameterType.Complex.Array
            OpenAPIV3Type.OBJECT -> ParameterType.Complex.Object
            OpenAPIV3Type.NULL -> null
        }

    private fun OpenAPIV3Reference.getReference() =
        this.ref.value
            .split("/")
            .getOrNull(3) ?: aigenticException("Wrong reference: ${this.ref.value}")

    private fun OpenAPIV3ParameterOrReference.resolve(): OpenAPIV3Parameter {
        fun OpenAPIV3Reference.resolveParameterObject(): OpenAPIV3Parameter =
            openApi.components?.parameters?.get(getReference())?.let {
                when (it) {
                    is OpenAPIV3Parameter -> it
                    is OpenAPIV3Reference -> it.resolveParameterObject()
                    else -> aigenticException("Unexpected parameter reference type: $it")
                }
            } ?: aigenticException("Cannot resolve $ref")

        return when (this) {
            is OpenAPIV3Parameter -> this
            is OpenAPIV3Reference -> this.resolveParameterObject()
            else -> aigenticException("Unexpected parameter or reference: $this")
        }
    }

    private fun OpenAPIV3SchemaOrReference.resolve(): OpenAPIV3Schema {
        fun OpenAPIV3Reference.resolveSchemaObject(): OpenAPIV3Schema =
            openApi.components?.schemas?.get(getReference())?.let {
                when (it) {
                    is OpenAPIV3Schema -> it
                    is OpenAPIV3Reference -> it.resolveSchemaObject()
                    else -> aigenticException("Unexpected schema reference type: $it")
                }
            } ?: aigenticException("Cannot resolve ref: $ref")

        return when (this) {
            is OpenAPIV3Reference -> this.resolveSchemaObject()
            is OpenAPIV3Schema -> this
            else -> aigenticException("Unexpected schema or reference: $this")
        }
    }

    private fun OpenAPIV3RequestBodyOrReference.resolve(): OpenAPIV3RequestBody {
        fun OpenAPIV3Reference.resolveRequestBody(): OpenAPIV3RequestBody =
            openApi.components?.requestBodies?.get(getReference())?.let {
                when (it) {
                    is OpenAPIV3RequestBody -> it
                    is OpenAPIV3Reference -> it.resolveRequestBody()
                    else -> aigenticException("Unexpected request body reference type: $it")
                }
            } ?: aigenticException("Cannot resolve ref: $ref")

        return when (this) {
            is OpenAPIV3RequestBody -> this
            is OpenAPIV3Reference -> this.resolveRequestBody()
            else -> aigenticException("Unexpected request body or reference: $this")
        }
    }
}

private fun community.flock.kotlinx.openapi.bindings.OpenAPIV3TypeDefinition?.toSingleType(): OpenAPIV3Type? =
    when (this) {
        null -> null
        is OpenAPIV3SingleType -> value
        is OpenAPIV3TypeArray -> values.firstOrNull { it != OpenAPIV3Type.NULL }
        else -> null
    }

data class Parameters(
    val query: List<Parameter> = listOf(),
    val path: List<Parameter> = listOf(),
)
