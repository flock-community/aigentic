package community.flock.aigentic.core.mcp

import community.flock.aigentic.core.tool.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.ClientOptions
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import io.modelcontextprotocol.kotlin.sdk.client.WebSocketClientTransport
import io.modelcontextprotocol.kotlin.sdk.shared.RequestOptions
import io.modelcontextprotocol.kotlin.sdk.shared.Transport
import kotlinx.io.*
import kotlinx.io.bytestring.encodeToByteString
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import java.io.File

/**
 * Real MCP Client implementation using the official SDK
 */
class McpClient internal constructor(
    private val config: McpConfig
) {
    private var client: Client? = null
    private var transport: Transport? = null
    private var isInitialized = false
    private val initMutex = Mutex()

    /**
     * Initialize and connect to the MCP server (lazy initialization)
     */
    internal suspend fun ensureInitialized() {
        initMutex.withLock {
            if (isInitialized) return

            // Create transport based on config
            transport = createTransport(config)

            // Create client
            client = Client(
                clientInfo = Implementation(
                    name = config.name,
                    version = config.version
                ),
                options = ClientOptions()
            )

            // Connect to server
            transport?.let { t ->
                client?.connect(t)
            }

            isInitialized = true
        }
    }

    /**
     * Get all available tools from the connected MCP server as Aigentic Tools
     */
    suspend fun getTools(): List<community.flock.aigentic.core.tool.Tool> {
        ensureInitialized()

        val mcpClient = client ?: return emptyList()

        // List available tools from MCP server
        val toolsResult = mcpClient.listTools(
            ListToolsRequest(),
            RequestOptions()
        ) ?: return emptyList()

        // Convert MCP tools to Aigentic tools
        return toolsResult.tools.map { mcpTool ->
            McpToolAdapter(mcpTool, mcpClient)
        }
    }

    /**
     * Close the connection to the MCP server
     */
    suspend fun close() {
        client?.close()
        transport = null
        client = null
        isInitialized = false
    }

    companion object {
        /**
         * Create an MCP client from configuration
         */
        fun fromConfig(config: McpConfig): McpClient {
            return McpClient(config)
        }

        /**
         * Create transport based on configuration
         */
        private fun createTransport(config: McpConfig): Transport {
            return when (config) {
                is McpConfig.Stdio -> {
                    // Create process for stdio transport
                    val processBuilder = ProcessBuilder().apply {
                        command(listOf(config.command) + config.args)
                        environment().putAll(config.env)
                    }
                    val process = processBuilder.start()

                    StdioClientTransport(
                        input = process.inputStream.asSource().buffered(),
                        output = process.outputStream.asSink().buffered()
                    )
                }
                is McpConfig.SSE -> {
                    val ktorClient = HttpClient(CIO)
                    SseClientTransport(
                        client = ktorClient,
                        urlString = config.url
                    )
                }
                is McpConfig.WebSocket -> {
                    val ktorClient = HttpClient(CIO)
                    WebSocketClientTransport(
                        client = ktorClient,
                        urlString = config.url
                    )
                }
                is McpConfig.Custom -> {
                    config.transport as Transport
                }
            }
        }
    }
}

/**
 * Adapter that converts an MCP Tool to an Aigentic Tool
 */
internal class McpToolAdapter(
    private val mcpTool: io.modelcontextprotocol.kotlin.sdk.Tool,
    private val client: Client
) : community.flock.aigentic.core.tool.Tool {
    override val name: ToolName = ToolName(mcpTool.name)
    override val description: String? = mcpTool.description

    override val parameters: List<Parameter> = parseInputSchemaToParameters(mcpTool.inputSchema)

    override val handler: suspend (toolArguments: JsonObject) -> String = { arguments ->
        // Convert JsonObject to Map<String, Any?>
        val argsMap = arguments.entries.associate { (key, value) ->
            key to when (value) {
                is JsonPrimitive -> {
                    when {
                        value.isString -> value.content
                        value.booleanOrNull != null -> value.boolean
                        value.intOrNull != null -> value.int
                        value.longOrNull != null -> value.long
                        value.floatOrNull != null -> value.float
                        value.doubleOrNull != null -> value.double
                        else -> value.content
                    }
                }
                is JsonArray -> value.map { element ->
                    when (element) {
                        is JsonPrimitive -> element.content
                        else -> element.toString()
                    }
                }
                is JsonObject -> value.entries.associate { it.key to it.value.toString() }
                is JsonNull -> null
            }
        }

        // Call the tool through MCP
        val result = client.callTool(
            name = mcpTool.name,
            arguments = argsMap,
            options = RequestOptions()
        )

        // Convert result to string
        result?.let { toolResult ->
            if (toolResult.isError == true) {
                // Handle error case
                toolResult.content.joinToString("\n") { content ->
                    when (content) {
                        is TextContent -> content.text ?: ""
                        else -> content.toString()
                    }
                }
            } else {
                // Handle success case
                toolResult.content.joinToString("\n") { content ->
                    when (content) {
                        is TextContent -> content.text ?: ""
                        is ImageContent -> "[Image: ${content.data}]"
                        is ResourceContents -> "[Resource]"
                        else -> content.toString()
                    }
                }
            }
        } ?: "No result from tool call"
    }
}

/**
 * Parse Tool.Input to Aigentic Parameters
 */
private fun parseInputSchemaToParameters(inputSchema: io.modelcontextprotocol.kotlin.sdk.Tool.Input): List<Parameter> {
    val properties = inputSchema.properties
    val required = inputSchema.required?.toSet() ?: emptySet()

    return properties.entries.map { (key, value) ->
        parseSchemaProperty(key, value.jsonObject, required.contains(key))
    }
}

/**
 * Parse JSON Schema to Aigentic Parameters (for nested objects)
 */
private fun parseJsonSchemaToParameters(schema: JsonObject): List<Parameter> {
    val type = schema["type"]?.jsonPrimitive?.content

    return when (type) {
        "object" -> {
            val properties = schema["properties"]?.jsonObject ?: return emptyList()
            val required = schema["required"]?.jsonArray?.mapNotNull {
                it.jsonPrimitive.content
            }?.toSet() ?: emptySet()

            properties.entries.map { (key, value) ->
                parseSchemaProperty(key, value.jsonObject, required.contains(key))
            }
        }
        else -> emptyList()
    }
}

private fun parseSchemaProperty(
    name: String,
    schema: JsonObject,
    isRequired: Boolean
): Parameter {
    val type = schema["type"]?.jsonPrimitive?.content ?: "string"
    val description = schema["description"]?.jsonPrimitive?.content

    return when (type) {
        "string" -> {
            val enumValues = schema["enum"]?.jsonArray?.mapNotNull {
                it.jsonPrimitive?.content
            }

            if (enumValues != null) {
                // Handle enum as a special case
                Parameter.Complex.Enum(
                    name = name,
                    description = description,
                    isRequired = isRequired,
                    default = null,
                    values = enumValues.map { PrimitiveValue.String(it) },
                    valueType = ParameterType.Primitive.String
                )
            } else {
                Parameter.Primitive(
                    name = name,
                    description = description,
                    isRequired = isRequired,
                    type = ParameterType.Primitive.String
                )
            }
        }
        "number" -> Parameter.Primitive(
            name = name,
            description = description,
            isRequired = isRequired,
            type = ParameterType.Primitive.Number
        )
        "integer" -> Parameter.Primitive(
            name = name,
            description = description,
            isRequired = isRequired,
            type = ParameterType.Primitive.Integer
        )
        "boolean" -> Parameter.Primitive(
            name = name,
            description = description,
            isRequired = isRequired,
            type = ParameterType.Primitive.Boolean
        )
        "array" -> {
            val items = schema["items"]?.jsonObject
            val itemParam = if (items != null) {
                parseSchemaProperty("", items, true)
            } else {
                Parameter.Primitive("", null, true, ParameterType.Primitive.String)
            }

            Parameter.Complex.Array(
                name = name,
                description = description,
                isRequired = isRequired,
                itemDefinition = itemParam
            )
        }
        "object" -> {
            val nestedParams = parseJsonSchemaToParameters(schema)
            Parameter.Complex.Object(
                name = name,
                description = description,
                isRequired = isRequired,
                parameters = nestedParams
            )
        }
        else -> Parameter.Primitive(
            name = name,
            description = description,
            isRequired = isRequired,
            type = ParameterType.Primitive.String
        )
    }
}