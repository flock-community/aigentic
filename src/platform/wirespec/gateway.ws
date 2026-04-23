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
    outputTokenCount: Integer,
    thinkingOutputTokenCount: Integer?,
    cachedInputTokenCount: Integer?
}

type ConfigDto {
    task: TaskDto,
    modelIdentifier: String,
    systemPrompt: String,
    tools: ToolDto[],
    exampleRunIds: String[]?,
    responseJsonSchema: String?
}

type TaskDto {
  description: String,
  instructions: String[]
}

type ToolDto {
    name: String,
    description: String?,
    parameters: ParameterDto[]
}

enum SenderDto {
    Agent,
    Model
}

enum MessageCategoryDto {
    SYSTEM_PROMPT,
    CONFIG_CONTEXT,
    RUN_CONTEXT,
    EXAMPLE,
    EXECUTION
}

enum ParameterTypeDto {
    STRING,
    NUMBER,
    INTEGER,
    BOOLEAN,
    ARRAY,
    ENUM,
    OBJECT
}
type ParameterDto = ParameterPrimitiveDto | ParameterObjectDto | ParameterArrayDto | ParameterEnumDto

type ParameterPrimitiveDto {
    name: String,
    description: String?,
    isRequired: Boolean,
    paramType: ParameterTypeDto
}

type ParameterObjectDto {
    name: String,
    description: String?,
    isRequired: Boolean,
    paramType: ParameterTypeDto,
    parameters: ParameterDto[]

}

type ParameterArrayDto {
    name: String,
    description: String?,
    isRequired: Boolean,
    paramType: ParameterTypeDto,
    itemDefinition: ParameterDto
}

type ParameterEnumDto {
    name: String,
    description: String?,
    isRequired: Boolean,
    paramType: ParameterTypeDto,
    default: PrimitiveValueDto?,
    values: PrimitiveValueDto[],
    valueType: ParameterTypeDto
}
type PrimitiveValueDto = PrimitiveValueStringDto | PrimitiveValueNumberDto | PrimitiveValueIntegerDto | PrimitiveValueBooleanDto

type PrimitiveValueStringDto {
    value: String
}

type PrimitiveValueNumberDto {
  value: Number
}

type PrimitiveValueIntegerDto {
  value: Integer
}
type PrimitiveValueBooleanDto {
  value: Boolean
}
enum PrimitiveValueTypeDto {
  STRING,
  NUMBER,
  INTEGER,
  BOOLEAN
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
    prompt: String,
    category: MessageCategoryDto?
}

type TextMessageDto {
    createdAt: String,
    sender: SenderDto,
    text: String,
    category: MessageCategoryDto?
}

type UrlMessageDto {
    createdAt: String,
    sender: SenderDto,
    url: String,
    mimeType: MimeTypeDto,
    category: MessageCategoryDto?
}

type Base64MessageDto {
    createdAt: String,
    sender: SenderDto,
    base64Content: String,
    mimeType: MimeTypeDto,
    category: MessageCategoryDto?
}

type ToolCallsMessageDto {
    createdAt: String,
    sender: SenderDto,
    toolCalls: ToolCallDto[],
    category: MessageCategoryDto?
}

type ToolCallDto {
    id: String,
    name: String,
    arguments: String,
    expectedArguments: String?
}

type ToolResultMessageDto {
    createdAt: String,
    sender: SenderDto,
    toolCallId: String,
    toolName: String,
    response: String,
    category: MessageCategoryDto?
}

type StructuredOutputMessageDto {
  createdAt: String,
  sender: SenderDto,
  response: String,
  category: MessageCategoryDto?
}

type MessageDto = SystemPromptMessageDto | TextMessageDto | UrlMessageDto | Base64MessageDto | ToolCallsMessageDto | ToolResultMessageDto | StructuredOutputMessageDto

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

endpoint GetRuns GET /gateway/runs ? { tags: String? } -> {
    200 -> RunDetailsDto[]
    404 -> Unit
    401 -> Unit
    500 -> ServerErrorDto
}
