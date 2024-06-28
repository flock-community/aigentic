type RunDto {
    startedAt: String,
    finishedAt: String,
    messages: MessageDto[],
    config: ConfigDto,
    result: ResultDto
}

type ConfigDto {
    name: String,
    description: String
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
    prompt: String
}

type TextMessageDto {
    text: String
}

type ImageUrlMessageDto {
    url: String,
    mimeType: MimeTypeDto
}

type ImageBase64MessageDto {
    base64Content: String,
    mimeType: MimeTypeDto
}

type ToolCallsMessageDto {
    toolCalls: ToolCallDto[]
}

type ToolCallDto {
    id: String,
    name: String,
    arguments: String
}

type MessageDto = SystemPromptMessageDto | TextMessageDto | ImageUrlMessageDto | ImageBase64MessageDto | ToolCallsMessageDto | ToolResultMessageDto

endpoint Gateway POST RunDto /gateway -> {
    201 -> Unit
}





