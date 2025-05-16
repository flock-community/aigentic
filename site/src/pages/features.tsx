import React from "react";
import Layout from "@theme/Layout";
import styles from "./contract.module.css";
import Heading from "@theme/Heading";
import Link from "@docusaurus/Link";
import clsx from "clsx";
import Tabs from "@theme/Tabs";
import TabItem from "@theme/TabItem";
import CodeBlock from "@theme/CodeBlock";

export default function FeaturesPage() {
  return (
    <Layout
      title="Aigentic Features"
      description="A Kotlin Multiplatform library for building and integrating AI agents into your applications"
      image="/img/code-snippet.jpg"
    >
      <div className={styles.mainContainer}>
        <main className={styles.contractMain}>
          <div id="kotlin-dsl">
            <section className={styles.hero} style={{ paddingTop: "4rem" }}>
              <h1>Kotlin DSL</h1>
              <p>
                Aigentic provides a powerful and intuitive Kotlin DSL for building AI agents.
                The DSL is designed to be expressive, type-safe, and easy to use, allowing developers
                to create complex AI agents with minimal boilerplate code.
              </p>
            </section>

            <section className={styles.section}>
              <h2>Key Features of the Kotlin DSL:</h2>

              <ol className={styles.principles}>
                <li>
                  <strong>Expressive and Concise:</strong>
                  <ul>
                    <li>
                      <span className={styles.bullet}></span> The Aigentic DSL allows you to define agents,
                      tools, and schemas in a natural, readable way.
                    </li>
                    <li>
                      <span className={styles.bullet}></span> Complex agent behaviors can be expressed
                      with minimal code, making your applications more maintainable.
                    </li>
                  </ul>
                </li>

                <li>
                  <strong>Type Safety:</strong>
                  <ul>
                    <li>
                      <span className={styles.bullet}></span> Leverage Kotlin's strong type system to catch
                      errors at compile time rather than runtime.
                    </li>
                    <li>
                      <span className={styles.bullet}></span> The DSL provides type-safe builders for defining
                      schemas, ensuring that your data structures are valid.
                    </li>
                  </ul>
                </li>

                <li>
                  <strong>Extensible and Modular:</strong>
                  <ul>
                    <li>
                      <span className={styles.bullet}></span> Create reusable components that can be shared
                      across different agents and applications.
                    </li>
                    <li>
                      <span className={styles.bullet}></span> Extend the DSL with your own custom builders
                      and functions to fit your specific needs.
                    </li>
                  </ul>
                </li>
              </ol>
            </section>
            <section
              className="card card-border-bottom card-nospace"
              style={{ marginTop: "2rem" }}
            >
              <CodeBlock
                language="kotlin"
                title="Agent Definition"
                className="custom-code-block"
              >
                {`// Create an agent with the Aigentic DSL
val agent = agent {
    // Configure the LLM provider
    provider = OpenAIProvider(
        apiKey = System.getenv("OPENAI_API_KEY"),
        model = "gpt-4o"
    )

    // Define the system prompt
    systemPrompt = """
        You are a helpful assistant that provides information about Kotlin programming.
        Be concise and provide code examples when appropriate.
    """

    // Add a calculator tool
    tool("calculator") {
        description = "Performs basic arithmetic calculations"

        input {
            property("expression", String::class) {
                description = "The arithmetic expression to evaluate"
            }
        }

        output {
            property("result", Double::class) {
                description = "The result of the calculation"
            }
        }

        handler { input ->
            val expression = input.get<String>("expression")
            val result = evaluateExpression(expression)
            mapOf("result" to result)
        }
    }
}`}
              </CodeBlock>
            </section>
          </div>

          <div id="providers">
            <section className={styles.generateSection}>
              <h2>LLM Providers</h2>
              <p>
                Aigentic is designed to be model-agnostic, allowing you to easily switch between
                different LLM providers without changing your application logic. This flexibility
                enables you to choose the right model for your specific use case, balancing factors
                like cost, performance, and capabilities.
              </p>
              <p>
                The provider system in Aigentic abstracts away the complexities of interacting with
                different LLM APIs, providing a consistent interface for your agents regardless of
                the underlying model. This makes it easy to experiment with different models or
                migrate to new providers as they become available.
              </p>
              <p>
                Aigentic supports multiple LLM providers out of the box, and the modular architecture
                makes it easy to add support for new providers as they emerge.
              </p>
              <Tabs>
                <TabItem value="OpenAI" label="OpenAI">
                  <CodeBlock language="kotlin">{`val openAIProvider = OpenAIProvider(
    apiKey = System.getenv("OPENAI_API_KEY"),
    model = "gpt-4o",
    temperature = 0.7,
    maxTokens = 2000
)

val agent = agent {
    provider = openAIProvider
    // Rest of agent configuration...
}`}</CodeBlock>
                </TabItem>

                <TabItem value="Gemini" label="Gemini">
                  <CodeBlock language="kotlin">{`val geminiProvider = GeminiProvider(
    apiKey = System.getenv("GEMINI_API_KEY"),
    model = "gemini-pro",
    temperature = 0.8,
    maxOutputTokens = 1024
)

val agent = agent {
    provider = geminiProvider
    // Rest of agent configuration...
}`}</CodeBlock>
                </TabItem>

                <TabItem value="Ollama" label="Ollama">
                  <CodeBlock language="kotlin">{`val ollamaProvider = OllamaProvider(
    baseUrl = "http://localhost:11434",
    model = "llama3",
    temperature = 0.5
)

val agent = agent {
    provider = ollamaProvider
    // Rest of agent configuration...
}`}</CodeBlock>
                </TabItem>
              </Tabs>
            </section>
          </div>

          <div id="tools">
            <section className={styles.validateSection}>
              <h2>Tools System</h2>
              <p>
                Tools are a core concept in Aigentic that extend an agent's capabilities beyond just
                conversation. They allow agents to perform specific actions, access external systems,
                and process data in structured ways.
              </p>

              <div className={styles.validateBlock}>
                <h3>Powerful Tool Definition</h3>
                <p>
                  Aigentic provides a flexible and powerful way to define tools using the Kotlin DSL.
                  Tools can be as simple or complex as needed, from basic calculators to sophisticated
                  integrations with external APIs and services. Each tool has a clear schema for inputs
                  and outputs, ensuring type safety and providing clear documentation for both developers
                  and AI models.
                </p>
              </div>

              <div className={styles.validateBlock}>
                <h3>Built-in Tools</h3>
                <p>
                  Aigentic comes with several built-in tools that you can use out of the box, including
                  HTTP tools for making web requests, OpenAPI tools for interacting with REST APIs, and
                  more. These tools can be easily added to your agents with minimal configuration,
                  allowing you to quickly build powerful AI applications.
                </p>
              </div>

              <div className={styles.validateBlock}>
                <h3>Tool Composition</h3>
                <p>
                  You can compose multiple tools together to create more complex capabilities. This
                  modular approach allows you to build sophisticated agents by combining simpler tools,
                  promoting code reuse and maintainability. Tools can be shared across different agents,
                  making it easy to create a library of reusable components for your organization.
                </p>
              </div>

              <div className={styles.validateBlock}>
                <h3>Advanced Tool Features</h3>
                <p>
                  Aigentic supports advanced tool features like streaming responses for long-running
                  operations and asynchronous tools for operations that take time to complete. These
                  features allow you to build responsive and efficient AI applications that can handle
                  complex tasks without blocking the user interface.
                </p>
              </div>

              <p>
                The tools system in Aigentic empowers you to create AI agents that can interact with
                the world in meaningful ways, going beyond simple text generation to perform real
                actions and solve complex problems.
              </p>
            </section>
          </div>
        </main>
      </div>
    </Layout>
  );
}
