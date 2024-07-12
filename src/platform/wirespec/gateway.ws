type RunDto {
    startedAt: String,
    finishedAt: String,
    config: ConfigDto,
    result: ResultDto,
    messages: MessageDto[],
    modelRequests: ModelRequestInfoDto[]
}

type ModelRequestInfoDto {
    startedAt: String,
    finishedAt: String,
    inputTokenCount: Integer,
    outputTokenCount: Integer
}

type ConfigDto {
    task: TaskDto,
    modelIdentifier: String,
    systemPrompt: String,
    tools: ToolDto[]
}

type TaskDto {
  description: String,
  instructions: String[]
}

type ToolDto {
    name: String,
    description: String,
    parameters: String
}

enum SenderDto {
    Aigentic,
    Model
}

type ToolResultMessageDto {
    toolCallId: String,
    toolName: String,
    response: String
}

type FinishedResultDto {
    description:String,
    response: String?
}

type StuckResultDto {
    reason:String
}

type FatalResultDto {
    message:String
}

type ResultDto = FinishedResultDto | StuckResultDto | FatalResultDto

enum MimeTypeDto {
    IMAGE_JPEG,
    IMAGE_PNG,
    IMAGE_WEBP,
    IMAGE_HEIC,
    IMAGE_HEIF
}

type SystemPromptMessageDto {
    sender: SenderDto,
    prompt: String
}

type TextMessageDto {
    sender: SenderDto,
    text: String
}

type ImageUrlMessageDto {
    sender: SenderDto,
    url: String,
    mimeType: MimeTypeDto
}

/*
 * TODO: rename to ImageBase64Message
 */
type ImageBase64MessageDto {
    sender: SenderDto,
    base64Content: String,
    mimeType: MimeTypeDto
}

type ToolCallsMessageDto {
    sender: SenderDto,
    toolCalls: ToolCallDto[]
}

type ToolCallDto {
    id: String,
    name: String,
    arguments: String
}

type GatewayClientErrorDto {
    message: String
}

type MessageDto = SystemPromptMessageDto | TextMessageDto | ImageUrlMessageDto | ImageBase64MessageDto | ToolCallsMessageDto

endpoint Gateway POST RunDto /gateway -> {
    201 -> Unit
    400 -> GatewayClientErrorDto
}
