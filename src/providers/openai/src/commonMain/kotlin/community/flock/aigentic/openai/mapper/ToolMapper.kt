package community.flock.aigentic.openai.mapper

import com.aallam.openai.api.chat.Tool
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.providers.jsonschema.emitPropertiesAndRequired
import kotlinx.serialization.json.put

internal fun ToolDescription.toOpenAITool(): Tool {
    val toolParameters =
        if (parameters.isEmpty()) {
            com.aallam.openai.api.core.Parameters.Empty
        } else {
            com.aallam.openai.api.core.Parameters.buildJsonObject {
                put("type", "object")
                emitPropertiesAndRequired(parameters)
            }
        }

    return Tool.function(
        name = name.value,
        description = description,
        parameters = toolParameters,
    )
}
