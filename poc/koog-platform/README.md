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
| `StructuredOutputMessageDto` | no distinct Koog type — structured output is an `Assistant` text driven by `LLMParams.schema` | ⚠️ policy mapping |

Two caveats:

1. **MIME-type subset.** Aigentic's `MimeTypeDto` is a fixed set — `IMAGE_{JPEG,PNG,WEBP,HEIC,HEIF}`
   and `APPLICATION_PDF`. Koog allows *any* `mimeType` string and also `Image`/`Video`/`Audio`/`File`
   sources. So a Koog attachment is publishable only when its MIME type is one of those six; anything
   else (video, audio, gif, …) has no representation in the current contract and is **skipped** by the
   mapper (`toMimeTypeDto()` returns `null`). Supporting more would require extending `MimeTypeDto` in
   `gateway.ws`. Koog's `Reasoning` parts (thinking) likewise have no Aigentic message type and are dropped.

2. **Structured output.** Aigentic has a dedicated `StructuredOutputMessageDto`, but Koog has no
   distinct structured-output message — it is a normal `Message.Assistant` whose text is JSON (produced
   via `LLMParams.schema`). It maps to `TextMessageDto` by default; emitting `StructuredOutputMessageDto`
   would be a policy choice (e.g. "when a response schema is configured, treat the final assistant
   message as structured output").

`KoogRunMapperTest` covers the supported types end-to-end and asserts the unsupported-MIME skip.

## Why this is an isolated build (important finding)

Koog `1.0.0` is built with **Kotlin 2.3.10**; Aigentic is on **Kotlin 2.1.10**. Kotlin metadata is
not forward-compatible, so a Kotlin-2.1.10 build cannot compile against Koog's 2.3.10 artifacts.
This PoC is therefore a **standalone Gradle build** (its own `settings.gradle.kts`, Kotlin 2.3.10)
that is *not* part of the main Aigentic build, so it does not affect `./gradlew build`.

`PlatformRunDto.kt` reproduces the `gateway.ws` contract locally for the same reason. A production
integration would instead reuse the Wirespec-generated `RunDto`/`Gateway` client from the published
`community.flock.aigentic:platform` artifact (which a Kotlin ≥ 2.3 consumer can read), or align the
Kotlin versions of the two projects.
