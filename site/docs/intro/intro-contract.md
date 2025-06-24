---
sidebar_position: 1
---

# DSL-First Approach

The **DSL-first approach** for AI agent design, as implemented by **Aigentic**, is a methodology that emphasizes the creation of a clear, type-safe definition of AI agents using a domain-specific language before any complex implementation occurs. This approach recognizes the importance of defining intuitive and expressive agent specifications as the cornerstone of building robust AI-powered applications. Here's a more detailed exploration of this concept:

### Key Principles of the DSL-First Approach:
1. **Declarative Agent Definition**:
    - At the heart of the DSL-first approach is the idea that a well-defined Kotlin DSL serves as the primary interface for creating and configuring AI agents.
    - These definitions are written in a **concise and expressive language**, making them accessible and understandable to developers, regardless of their expertise in AI or LLM technologies.

2. **Provider Independence**:
    - The agent definition is **agnostic** of any specific LLM provider, allowing seamless switching between OpenAI, Gemini, Ollama, and other providers.
    - By abstracting the agent definition from provider-specific details, developers are free to use different LLM backends while maintaining consistent agent behavior and capabilities.

3. **Type-Safe Tool Integration**:
    - This approach simplifies the integration of custom tools and capabilities into AI agents through a type-safe interface.
    - The DSL serves as a **structured framework**, ensuring that tool inputs and outputs are properly defined and validated. This reduces errors and improves reliability when agents interact with external systems and APIs.
