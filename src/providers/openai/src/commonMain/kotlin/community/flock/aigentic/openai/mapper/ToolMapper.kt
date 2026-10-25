package community.flock.aigentic.openai.mapper

import com.aallam.openai.api.chat.Tool
import com.aallam.openai.api.core.Parameters
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.providers.jsonschema.SchemaDialect
import community.flock.aigentic.providers.jsonschema.toObjectSchema

internal fun ToolDescription.toOpenAITool(): Tool {
    val toolParameters =
        if (parameters.isEmpty()) {
            Parameters.Empty
        } else {
            Parameters(parameters.toObjectSchema(SchemaDialect.OPENAI_JSON_SCHEMA))
        }

    return Tool.function(
        name = name.value,
        description = description,
        parameters = toolParameters,
    )
}
