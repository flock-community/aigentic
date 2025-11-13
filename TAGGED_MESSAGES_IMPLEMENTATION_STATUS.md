# Tagged Messages Architecture - Implementation Status

## Overview

This document tracks the implementation of the Tagged Messages Architecture for the Aigentic project. This architecture replaces the separate message flows approach with a unified, category-based system.

## Completed: Phase 1 - Add Category Infrastructure ✅

**Commit:** e18851d - "Phase 1: Add MessageCategory infrastructure"

### Changes Made:

1. **Created `MessageCategory.kt`** - New enum with 5 categories:
   - `SYSTEM_PROMPT`: System prompt that defines agent behavior
   - `CONFIG_CONTEXT`: Context from agent.context { } configuration
   - `RUN_CONTEXT`: Attachments provided at run start
   - `EXAMPLE`: Messages from example runs (few-shot learning)
   - `EXECUTION`: Runtime execution messages (tool calls, results)

2. **Updated `Message.kt`**:
   - Replaced `messageType: MessageType` with `category: MessageCategory`
   - Added `withCategory()` abstract method to all Message subclasses
   - Removed `MessageType` enum (New/Example)
   - All message types now have appropriate default categories

3. **Updated Message Creation Sites**:
   - `Context.toMessage()`: Sets `CONFIG_CONTEXT` category
   - `Attachment.toMessage()`: Sets `RUN_CONTEXT` category
   - `createTaskInputMessage()`: Sets `RUN_CONTEXT` category
   - `correctionMessage`: Sets `EXECUTION` category
   - `toExampleMessage()`: Sets `EXAMPLE` category

4. **Updated Core Files**:
   - `AgentExecutor.kt`: Updated all message creation to use categories
   - `State.kt`: Changed filtering from `MessageType.New` to exclude `MessageCategory.EXAMPLE`
   - `CorrectionMessage.kt`: Updated to use `MessageCategory.EXECUTION`

5. **Updated Tests**:
   - `AgentExecutorTest.kt`: Updated test assertions to use categories
   - Platform and provider test files updated

### Benefits of Phase 1:

- ✅ **Backward Compatible**: Default categories maintain existing behavior
- ✅ **Type Safe**: Categories are compile-time checked
- ✅ **Self-Documenting**: Message purpose is explicit
- ✅ **Foundation**: Ready for Phase 2 refactoring

---

## Phase 2 - Refactor State (Current Branch Implementation)

### Current State Structure (Before send-attachments PR):

```kotlin
data class State(
    val startedAt: Instant,
    var finishedAt: Instant? = null,
    val messages: MutableSharedFlow<Message> = MutableSharedFlow(replay = 1000),
    val events: MutableSharedFlow<AgentStatus> = MutableSharedFlow(replay = 1000),
    val modelRequestInfos: MutableSharedFlow<ModelRequestInfo> = MutableSharedFlow(replay = 1000),
    val exampleRunIds: MutableSharedFlow<RunId> = MutableSharedFlow(replay = 1000),
)
```

### Recommendation:

**Keep the current simple State structure!**

The current branch already has a single `messages` flow. With Phase 1 complete (categories), we now have the best of both worlds:

1. **Single source of truth**: One `messages` flow
2. **Easy filtering**: `messages.replayCache.filter { it.category == ... }`
3. **No extra complexity**: No need for separate flows
4. **Backward compatible**: Existing code works as-is

### What Needs to be Done for Phase 2:

Since the current State already uses a single flow, Phase 2 is mostly about adding convenience methods:

1. **Add filtering helper functions to State.kt**:
   ```kotlin
   fun State.getMessages(vararg categories: MessageCategory): List<Message>
   fun State.getMessagesByCategory(category: MessageCategory): List<Message>
   fun State.getSystemPrompt(): Message.SystemPrompt?
   fun State.getModelMessages(): List<Message> // Excludes EXAMPLE
   ```

2. **Keep existing State extension functions** but ensure they work with categories:
   - `addMessage()`, `addMessages()` - Already work!
   - No changes needed to the core State structure

---

## Phase 3 - Update Platform Mappers (Needs Work)

### Current send-attachments PR Approach:

The `send-attachments-and-response-schema-to-platform` branch has:
- Separate fields in `AgentRun`: `systemPromptMessage`, `configContextMessages`, etc.
- Separate DTO fields in wirespec: `contextMessages`, `runAttachmentMessages`

### Tagged Messages Approach:

With categories, we can simplify:

```kotlin
// In RequestMapper.kt
fun <I, O> AgentRun<O>.toDto(...): RunDto {
    val systemPrompt = messages
        .filterIsInstance<Message.SystemPrompt>()
        .firstOrNull()?.prompt ?: ""

    val configContextMessages = messages
        .filter { it.category == MessageCategory.CONFIG_CONTEXT }

    val runAttachmentMessages = messages
        .filter { it.category == MessageCategory.RUN_CONTEXT }

    val executionMessages = messages
        .filter { it.category == MessageCategory.EXECUTION }

    return RunDto(
        config = ConfigDto(
            systemPrompt = systemPrompt,
            contextMessages = configContextMessages.mapNotNull { it.toDto() },
            // ...
        ),
        runAttachmentMessages = runAttachmentMessages.mapNotNull { it.toDto() },
        executionMessages = executionMessages.mapNotNull { it.toDto() },
        // ...
    )
}
```

### Decision Point:

Should `AgentRun` have:

**Option A: Single messages list with categories (Recommended)**
```kotlin
data class AgentRun<O : Any>(
    val messages: List<Message>, // All messages with categories
    // ... other fields
) {
    // Computed properties
    val systemPrompt: String?
        get() = messages.filterIsInstance<Message.SystemPrompt>().firstOrNull()?.prompt

    val configContextMessages: List<Message>
        get() = messages.filter { it.category == CONFIG_CONTEXT }
    // etc.
}
```

**Option B: Separate fields (Current PR approach)**
```kotlin
data class AgentRun<O : Any>(
    val messages: List<Message>,
    val systemPromptMessage: Message.SystemPrompt,
    val configContextMessages: List<Message>,
    val runAttachmentMessages: List<Message>,
    val executionMessages: List<Message>,
    // ...
)
```

**Recommendation: Option A** - Simpler, more maintainable, extensible.

---

## Phase 4 - Cleanup & Documentation (Needs Work)

### Tasks:

1. **Update AgentRun data class** (choose Option A or B)
2. **Create migration guide** for library users
3. **Add tests** for message categorization
4. **Performance testing** to ensure no regression
5. **Update documentation** about message categories

---

## Key Decision: What to do about the send-attachments PR?

### Option 1: Complete Tagged Messages Implementation (Recommended)

**Pros:**
- Better long-term architecture
- More maintainable
- Easier to extend
- Simpler code

**Cons:**
- Different from the reviewed PR
- Need to update platform integration

**Steps:**
1. Complete Phase 2-4 on this branch
2. Update platform mappers to use filtering
3. Test thoroughly
4. Replace the send-attachments PR with this implementation

### Option 2: Hybrid Approach

**Keep send-attachments PR structure but with categories:**
- AgentRun has separate fields
- But uses categories internally
- Platform mappers stay the same

**Pros:**
- Smaller change from reviewed PR
- Platform integration unchanged

**Cons:**
- Maintains redundancy (messages + separate fields)
- Not as clean architecturally

### Option 3: Two-Step Migration

1. **Merge send-attachments PR as-is**
2. **Then migrate to tagged messages** in a follow-up PR

**Pros:**
- Delivers immediate value
- Less risky

**Cons:**
- More work overall
- Temporary technical debt

---

## Recommendation

I recommend **Option 1**: Complete the Tagged Messages implementation now.

**Rationale:**
1. Phase 1 is already done (categories exist)
2. Current State already uses single flow (no multi-flow complexity)
3. Cleaner architecture from the start
4. Platform mappers just need filtering logic

**Next Steps:**
1. Add helper methods to State (Phase 2) - 1 hour
2. Simplify AgentRun to use Option A (Phase 3) - 2 hours
3. Update platform mappers to filter by category (Phase 3) - 2 hours
4. Add tests and documentation (Phase 4) - 3 hours

**Total: ~8 hours of additional work**

---

## Testing Status

❌ **Cannot run tests due to network issues** (Gradle download failing)

Once network is available:
```bash
./gradlew --no-build-cache clean :src:core:jvmTest :src:platform:jvmTest
```

---

## Files Changed in Phase 1

1. `src/core/src/commonMain/kotlin/community/flock/aigentic/core/message/MessageCategory.kt` (new)
2. `src/core/src/commonMain/kotlin/community/flock/aigentic/core/message/Message.kt`
3. `src/core/src/commonMain/kotlin/community/flock/aigentic/core/agent/AgentExecutor.kt`
4. `src/core/src/commonMain/kotlin/community/flock/aigentic/core/agent/message/CorrectionMessage.kt`
5. `src/core/src/commonMain/kotlin/community/flock/aigentic/core/agent/state/State.kt`
6. `src/core/src/jvmTest/kotlin/community/flock/aigentic/core/agent/AgentExecutorTest.kt`
7. `src/platform/src/commonMain/kotlin/community/flock/aigentic/platform/mapper/ResponseMapper.kt`
8. `src/platform/src/jvmTest/kotlin/community/flock/aigentic/platform/testing/TestExecutorTest.kt`
9. `src/providers/gemini/src/jvmTest/kotlin/community/flock/aigentic/gemini/mapper/GeminiRequestMapperKtTest.kt`
10. `src/providers/openai/src/commonMain/kotlin/community/flock/aigentic/openai/mapper/MessageMappers.kt`
11. `src/providers/openai/src/jvmTest/kotlin/community/flock/aigentic/openai/mapper/OpenAIMapperTest.kt`

---

## Next Actions Required

1. **Decide**: Which option (1, 2, or 3) to pursue?
2. **If Option 1**: Complete Phases 2-4 (~8 hours)
3. **Test**: Run full test suite when network available
4. **Review**: Platform integration requirements
5. **Merge**: Replace send-attachments PR or continue work there

---

Last Updated: 2025-01-13
Status: Phase 1 Complete, Phases 2-4 Pending Decision
