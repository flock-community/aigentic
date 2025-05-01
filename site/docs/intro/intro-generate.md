# Create and Execute Agents

The vision of agent creation in Aigentic centers on enabling a DSL-first approach where the Kotlin DSL acts as the expressive interface for defining AI capabilities. By prioritizing the declarative definition over low-level implementation details, AI integration is streamlined, reducing complexity and fostering better focus on the actual business logic.

Agents, defined through the DSL, play a pivotal role in this vision. They are designed to encapsulate specific AI behaviors and capabilities, remaining adaptable to different LLM providers and independent of specific model constraints. This abstraction allows agents to act as flexible components, transcending changes in underlying LLM technologies while maintaining consistent behavior in your application.

The Aigentic execution engine helps bridge the gap between agent definition and LLM interaction by handling the complexities of prompt engineering, context management, and tool integration. This approach ensures that agents behave predictably across different providers, managing conversations, tool calls, and response generation in a consistent and reliable manner.
