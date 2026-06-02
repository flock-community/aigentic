# PoC: publishing Koog runs to the Aigentic Platform

This proof-of-concept answers the question: **can [JetBrains Koog](https://github.com/JetBrains/koog)
publish its agent runs to the Aigentic Platform, using the same contract Aigentic itself uses?**

**Answer: yes.** No fork or patch of Koog is required — Koog exposes public extension points
that surface everything the platform contract needs.

## How Aigentic publishes a run (the contract we target)

After every run, Aigentic calls `publishRun` (`src/core/.../agent/AgentExecutor.kt`) which,
when a `platform { … }` is configured, does a single authenticated request:

```
POST RunDto  ->  /gateway/runs        (HTTP Basic Auth; 201 / 401 / 400 / 500)
```

`RunDto` is defined in `src/platform/wirespec/gateway.ws` and carries `startedAt`, `finishedAt`,
`config` (task, model identifier, system prompt, tools, temperature, …), `result`
(Finished / Stuck / Fatal), `messages[]`, and `modelRequests[]` (per-LLM-call token usage).

Nothing about publishing is runtime-specific: it is just "build a `RunDto` and POST it".

## How this PoC does the same from Koog

Koog's `agents-features-event-handler` feature lets you hook the full agent lifecycle. We install
it via a small extension, `aigenticPlatform(name, secret, …)`, which uses `handleEvents { … }` to
collect the run and POST a `RunDto` on completion — mirroring Aigentic's `publishRun`:

```kotlin
val agent = AIAgent(promptExecutor = executor, llmModel = model, systemPrompt = "…") {
    aigenticPlatform(name = "…", secret = "…")   // <- publishes the run on completion
}
agent.run("…")   // on completion a RunDto is POSTed to /gateway/runs
```

Hooks used (`AigenticPlatformExporter.kt`):
- `onAgentStarting` — run start time.
- `onLLMCallStarting` / `onLLMCallCompleted` — full `Prompt` (message history), `LLModel`,
  tool descriptors, the `Message.Assistant` response and its `ResponseMetaInfo` token counts →
  one `modelRequests` entry per LLM call.
- `onAgentCompleted` / `onAgentExecutionFailed` → `Finished` / `Fatal` result, then the POST.

`KoogRunMapper.kt` maps Koog's `Message`/`MessagePart`/`ToolDescriptor` types onto the Aigentic
DTOs in `PlatformRunDto.kt`.

## What the test proves

`AigenticPlatformExporterTest` runs a **real Koog agent** (LLM mocked with Koog's `getMockExecutor`,
the platform mocked with a Ktor `MockEngine`), then asserts that exactly one `RunDto` was POSTed to
`/gateway/runs` containing the system prompt, the assistant response, a `modelRequests` entry, and a
`FinishedResultDto`.

```bash
# from the repository root
./gradlew -p poc/koog-platform test
```

## Message-type compatibility (Koog ↔ Aigentic Platform)

The Aigentic `MessageDto` union and Koog's message model line up well. Mapping
(`KoogRunMapper.kt`):

| Aigentic `MessageDto`        | Koog source                                                        | Status |
|------------------------------|--------------------------------------------------------------------|--------|
| `SystemPromptMessageDto`     | `Message.System`                                                   | ✅ 1:1 |
| `TextMessageDto`             | `MessagePart.Text` (User → Agent, Assistant → Model)               | ✅ 1:1 |
| `ToolCallsMessageDto`        | `MessagePart.Tool.Call` (on `Message.Assistant`)                   | ✅ 1:1 |
| `ToolResultMessageDto`       | `MessagePart.Tool.Result` (on `Message.User`)                      | ✅ 1:1 |
| `UrlMessageDto`              | `MessagePart.Attachment` + `AttachmentContent.URL`                 | ✅ with MIME caveat |
| `Base64MessageDto`           | `MessagePart.Attachment` + `AttachmentContent.Binary` (`asBase64()`) | ✅ with MIME caveat |
| `StructuredOutputMessageDto` | the final `Message.Assistant` when the agent has a typed (non-`String`) output | ✅ (see Structured output) |

Two caveats:

1. **MIME-type subset.** Aigentic's `MimeTypeDto` is a fixed set — `IMAGE_{JPEG,PNG,WEBP,HEIC,HEIF}`
   and `APPLICATION_PDF`. Koog allows *any* `mimeType` string and also `Image`/`Video`/`Audio`/`File`
   sources. So a Koog attachment is publishable only when its MIME type is one of those six; anything
   else (video, audio, gif, …) has no representation in the current contract and is **skipped** by the
   mapper (`toMimeTypeDto()` returns `null`). Supporting more would require extending `MimeTypeDto` in
   `gateway.ws`. Koog's `Reasoning` parts (thinking) likewise have no Aigentic message type and are dropped.

2. **Structured output.** Aigentic has a dedicated `StructuredOutputMessageDto`. Koog has no distinct
   structured-output *message* type, but it fully supports structured output as a first-class agent
   feature (see below). The structured result is captured as the run's result; at the message level it
   is the final `Message.Assistant` text (JSON).

`KoogRunMapperTest` covers the supported types end-to-end and asserts the unsupported-MIME skip.

## Structured output (typed request/response, e.g. "PDF invoice → line items → done")

Yes — this is fully supported in Koog and is arguably *more* first-class than in Aigentic. Koog gives
a typed `AIAgent<Input, Output>` whose `run(...)` returns the parsed object directly:

```kotlin
@Serializable
@LLMDescription("An invoice with its line items")
data class Invoice(
    @property:LLMDescription("Invoice number") val number: String,
    @property:LLMDescription("Total amount due") val total: Double,
    val items: List<LineItem>,
) { @Serializable data class LineItem(val description: String, val amount: Double) }

val extractInvoice = strategy<String, Invoice>("invoice-extraction") {
    val extract by nodeLLMRequestStructured<Invoice>()   // schema is derived from the @Serializable type
    edge(nodeStart forwardTo extract)
    edge(extract forwardTo nodeFinish transformed { it.getOrThrow().data })
}

val agent = AIAgent(promptExecutor = executor, llmModel = model, strategy = extractInvoice) {
    aigenticPlatform(name = "…", secret = "…")
}
val invoice: Invoice = agent.run("…")   // typed result, then the run is published
```

How it works under the hood (`PromptExecutor.executeStructured` / `requestLLMStructured<T>`): Koog
derives a JSON schema from the `@Serializable` type, uses the model's native structured-output mode when
available (`LLMCapability.Schema.JSON.Standard/Basic`) or otherwise a manual mode (schema in the prompt),
then parses the assistant's JSON back into `T` (with an optional `StructureFixingParser` to repair
malformed output). This is the direct equivalent of Aigentic's `Outcome.Finished` carrying a typed `O`.

**Feeding a PDF in:** attach the document to the user message and request the structure in one node:

```kotlin
val extract by node<String, Result<StructuredResponse<Invoice>>> {
    llm.writeSession {
        appendPrompt {
            user("Extract the invoice") {
                attachments {
                    binaryFile(pdfBytes, format = "pdf", mimeType = "application/pdf")
                }
            }
        }
        requestLLMStructured<Invoice>()
    }
}
```

**Feature parity with Aigentic.** Aigentic treats an agent as a *structured-output agent* when it has a
typed response and no tools (`isStructuredOutputAgent()` = `tools.isEmpty() && responseParameter != null`),
in which case the final answer is published as a `StructuredOutputMessageDto` and `config.responseJsonSchema`
is set. The exporter mirrors this: it detects structured output from the agent's typed result
(`onAgentCompleted.result !is String`) and then

- emits the final assistant message as `StructuredOutputMessageDto` (Model sender) rather than `TextMessageDto`,
- sets `FinishedResultDto.response` to the structured JSON,
- sets `config.responseJsonSchema` from `prompt.params.schema` when the model uses native structured output.

`StructuredOutputExporterTest` runs the full PDF-invoice flow end-to-end (mocked LLM): a real PDF
`MessagePart.Attachment` is sent in the user message, and the published `RunDto` is asserted to contain a
`Base64MessageDto` (`APPLICATION_PDF`) for the document and a `StructuredOutputMessageDto` carrying the typed
invoice — the same shape an equivalent Aigentic run would publish.

## Message categories (`context` vs `start()` vs execution)

Aigentic stamps each message with a `MessageCategoryDto` based on *where it came from*
(`AgentExecutor.seedInitialMessages`):

| Category         | Aigentic origin                                   | Koog equivalent |
|------------------|---------------------------------------------------|-----------------|
| `SYSTEM_PROMPT`  | the system prompt                                 | `Message.System` |
| `CONFIG_CONTEXT` | `context { … }` (set at agent definition, every run) | the base `AIAgentConfig.prompt` (system + seeded context) |
| `RUN_CONTEXT`    | `start(input, attachments)` (per invocation)      | content added for the run, before the first model turn |
| `EXECUTION`      | messages produced during the loop                 | everything from the first `Message.Assistant` onward |
| `EXAMPLE`        | example runs                                       | (set explicitly via metadata) |

**Yes — you can add attachments in different places in Koog, and they are distinguishable.** A PDF can
live in (a) the **config prompt** (`AIAgentConfig.prompt`, the analog of `context { }` — present on every
run), (b) the **run input** (appended by a node from `agent.run(input)`, the analog of `start()`), or (c)
**mid-execution**. The exporter derives the category from two signals:

1. **Origin (automatic).** The base config prompt is reachable from the event context
   (`agent.agentConfig.prompt`), so the exporter records its size at `onAgentStarting`. Messages within
   that prefix are `SYSTEM_PROMPT`/`CONFIG_CONTEXT`; content appended before the first assistant message is
   `RUN_CONTEXT`; everything after is `EXECUTION`.
2. **Explicit metadata (override).** Koog messages carry `metaInfo.metadata: JsonObject?`. Tagging a
   message with `{"aigentic.category": "RUN_CONTEXT"}` (etc.) overrides the positional default — the direct
   analog of Aigentic choosing the category from the API you call.

`MessageCategoryTest` covers both signals; `StructuredOutputExporterTest` proves it end-to-end by attaching
a reference image in the config prompt (→ `CONFIG_CONTEXT`) and the invoice PDF in the run (→ `RUN_CONTEXT`)
and asserting the published `RunDto` distinguishes them.

## Why this is an isolated build (important finding)

Koog `1.0.0` is built with **Kotlin 2.3.10**; Aigentic is on **Kotlin 2.1.10**. Kotlin metadata is
not forward-compatible, so a Kotlin-2.1.10 build cannot compile against Koog's 2.3.10 artifacts.
This PoC is therefore a **standalone Gradle build** (its own `settings.gradle.kts`, Kotlin 2.3.10)
that is *not* part of the main Aigentic build, so it does not affect `./gradlew build`.

`PlatformRunDto.kt` reproduces the `gateway.ws` contract locally for the same reason. A production
integration would instead reuse the Wirespec-generated `RunDto`/`Gateway` client from the published
`community.flock.aigentic:platform` artifact (which a Kotlin ≥ 2.3 consumer can read), or align the
Kotlin versions of the two projects.
