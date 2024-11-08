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
    description: String?,
    parameters: String
}

enum SenderDto {
    Agent,
    Model
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
    IMAGE_HEIF,
    APPLICATION_PDF
}

type SystemPromptMessageDto {
    createdAt: String,
    sender: SenderDto,
    prompt: String
}

type TextMessageDto {
    createdAt: String,
    sender: SenderDto,
    text: String
}

type UrlMessageDto {
    createdAt: String,
    sender: SenderDto,
    url: String,
    mimeType: MimeTypeDto
}

type Base64MessageDto {
    createdAt: String,
    sender: SenderDto,
    base64Content: String,
    mimeType: MimeTypeDto
}

type ToolCallsMessageDto {
    createdAt: String,
    sender: SenderDto,
    toolCalls: ToolCallDto[]
}

type ToolCallDto {
    id: String,
    name: String,
    arguments: String
}

type ToolResultMessageDto {
    createdAt: String,
    sender: SenderDto,
    toolCallId: String,
    toolName: String,
    response: String
}

type MessageDto = SystemPromptMessageDto | TextMessageDto | UrlMessageDto | Base64MessageDto | ToolCallsMessageDto | ToolResultMessageDto

type GatewayClientErrorDto {
    message: String
}

type ServerErrorDto {
    name: String,
    description: String
}

endpoint Gateway POST RunDto /gateway/runs -> {
    201 -> Unit
    401 -> Unit
    400 -> GatewayClientErrorDto
    500 -> ServerErrorDto
}

type RunDetailsDto {
    runId: String,
    startedAt: String,
    finishedAt: String,
    result: ResultDto,
    duration: String,
    messages: MessageDto[],
    tags: String[]
}

endpoint GetRuns GET /gateway/runs ? { tags: String } -> {
    200 -> RunDetailsDto[]
    404 -> Unit
    401 -> Unit
    500 -> ServerErrorDto
}
