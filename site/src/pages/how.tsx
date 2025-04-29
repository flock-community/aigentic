import React from "react";
import Layout from "@theme/Layout";
import styles from "./contract.module.css";
import Heading from "@theme/Heading";
import Link from "@docusaurus/Link";
import clsx from "clsx";
import Tabs from "@theme/Tabs";
import TabItem from "@theme/TabItem";
import CodeBlock from "@theme/CodeBlock";

export default function ContractPage() {
  return (
    <Layout
      title="Design AI Agents with Aigentic"
      description="Simplify your AI agent development workflows, accelerate implementation, and guarantee effective AI interactions with Aigentic's design-first approach"
      image="/img/code-snippet.jpg"
    >
      <div className={styles.mainContainer}>
        <main className={styles.contractMain}>
          <div id="contract">
            <section className={styles.hero} style={{ paddingTop: "4rem" }}>
              <h1>Design-First</h1>
              <p>
                The design-first approach for AI agents, as envisioned
                by Aigentic, is a methodology that emphasizes the creation of a
                comprehensive agent design before any implementation occurs. This
                approach recognizes the importance of defining clear and
                detailed agent behaviors and capabilities as the cornerstone of
                building effective AI interactions. Here's a more detailed exploration
                of this concept:
              </p>
            </section>

            <section className={styles.section}>
              <h2>Key Principles of the Design-First Approach for AI Agents:</h2>

              <ol className={styles.principles}>
                <li>
                  <strong>Single Source of Truth:</strong>
                  <ul>
                    <li>
                      <span className={styles.bullet}></span> At the heart of
                      the design-first approach is the idea that a
                      well-defined agent design serves as the single authoritative
                      source of truth for the AI agent's behavior.
                    </li>
                    <li>
                      <span className={styles.bullet}></span> These
                      designs are expressed in an intuitive Kotlin DSL, making them accessible and understandable across
                      teams, regardless of their technical expertise with AI technologies.
                    </li>
                  </ul>
                </li>

                <li>
                  <strong>Independence from LLM Providers:</strong>
                  <ul>
                    <li>
                      <span className={styles.bullet}></span> The agent
                      design is agnostic of any specific LLM provider, model version,
                      or deployment environment.
                    </li>
                    <li>
                      <span className={styles.bullet}></span> By abstracting the
                      design from implementation details, teams are free
                      to deploy agents with different LLM providers while
                      maintaining consistency and adherence to the defined
                      behaviors.
                    </li>
                  </ul>
                </li>

                <li>
                  <strong>Collaborative Design–Centric Methodology:</strong>
                  <ul>
                    <li>
                      <span className={styles.bullet}></span> This approach
                      simplifies communication between cross–functional teams
                      (e.g., developers, AI specialists, product managers, and
                      quality assurance teams).
                    </li>
                    <li>
                      <span className={styles.bullet}></span> Agent designs
                      serve as a shared reference point, ensuring everyone is
                      aligned on expectations and requirements for the
                      AI interactions. This reduces ambiguity and miscommunication
                      during the development lifecycle.
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
                title="TodoAgent.kt"
                className="custom-code-block"
              >
                {`import community.flock.aigentic.core.agent.Agent
import community.flock.aigentic.core.agent.tool.CoreTools
import community.flock.aigentic.core.agent.tool.Tool
import community.flock.aigentic.providers.openai.OpenAIProvider

data class Todo(val id: Int, val name: String)

class TodoRepository {
    private val todos = mutableListOf<Todo>()

    fun getAllTodos(): List<Todo> = todos.toList()

    fun getTodoById(id: Int): Todo? = todos.find { it.id == id }

    fun createTodo(name: String): Todo {
        val id = (todos.maxOfOrNull { it.id } ?: 0) + 1
        val todo = Todo(id, name)
        todos.add(todo)
        return todo
    }

    fun updateTodo(id: Int, name: String): Todo? {
        val index = todos.indexOfFirst { it.id == id }
        if (index == -1) return null
        val updatedTodo = Todo(id, name)
        todos[index] = updatedTodo
        return updatedTodo
    }

    fun deleteTodo(id: Int): Boolean {
        val removed = todos.removeIf { it.id == id }
        return removed
    }
}`}
              </CodeBlock>
            </section>
          </div>

          <div id="create">
            <section className={styles.generateSection}>
              <h2>Create</h2>
              <p>
                The vision of agent creation centers on enabling a
                design-first approach where agent designs act as the single
                source of truth. By prioritizing the design over the
                implementation, development is streamlined, reducing ambiguity
                and fostering better collaboration among cross-functional teams.
              </p>
              <p>
                AI agent designs, often mapped to specific use cases, play a pivotal role in
                this vision. They are designed to have a longer lifespan
                compared to implementation code and must remain isolated to
                ensure they are reusable, adaptable, and independent of specific
                LLM providers. This isolation allows agent designs to
                act as durable blueprints, transcending implementation changes
                and maintaining a stable foundation for the system’s evolution.
              </p>
              <p>
                Aigentic's Kotlin DSL helps bridge the gap between design and
                implementation by providing an intuitive way to define AI agents with their tools,
                behaviors, and capabilities. This approach ensures that the created agents precisely reflect
                the defined design, capturing all possible interactions in a consistent and predictable manner.
              </p>
              <Tabs>
                <TabItem value="OpenAI" label="OpenAI">
                  <CodeBlock language="kotlin">{`val todoRepository = TodoRepository()

val todoAgent = Agent.create {
    provider = OpenAIProvider(
        apiKey = System.getenv("OPENAI_API_KEY"),
        model = "gpt-4o"
    )

    tools {
        tool("getAllTodos") {
            description = "Get all todos"
            execute { todoRepository.getAllTodos() }
        }

        tool("getTodoById") {
            description = "Get a todo by ID"
            parameters {
                parameter<Int>("id") { description = "The ID of the todo" }
            }
            execute { params -> todoRepository.getTodoById(params["id"]) }
        }

        tool("createTodo") {
            description = "Create a new todo"
            parameters {
                parameter<String>("name") { description = "The name of the todo" }
            }
            execute { params -> todoRepository.createTodo(params["name"]) }
        }
    }
}`}</CodeBlock>
                </TabItem>

                <TabItem value="Gemini" label="Gemini">
                  <CodeBlock language="kotlin">{`val todoRepository = TodoRepository()

val todoAgent = Agent.create {
    provider = GeminiProvider(
        apiKey = System.getenv("GEMINI_API_KEY"),
        model = "gemini-pro"
    )

    tools {
        tool("getAllTodos") {
            description = "Get all todos"
            execute { todoRepository.getAllTodos() }
        }

        tool("getTodoById") {
            description = "Get a todo by ID"
            parameters {
                parameter<Int>("id") { description = "The ID of the todo" }
            }
            execute { params -> todoRepository.getTodoById(params["id"]) }
        }

        tool("createTodo") {
            description = "Create a new todo"
            parameters {
                parameter<String>("name") { description = "The name of the todo" }
            }
            execute { params -> todoRepository.createTodo(params["name"]) }
        }
    }
}`}</CodeBlock>
                </TabItem>

                <TabItem value="Ollama" label="Ollama">
                  <CodeBlock language="kotlin">{`val todoRepository = TodoRepository()

val todoAgent = Agent.create {
    provider = OllamaProvider(
        baseUrl = "http://localhost:11434",
        model = "llama3"
    )

    tools {
        tool("getAllTodos") {
            description = "Get all todos"
            execute { todoRepository.getAllTodos() }
        }

        tool("getTodoById") {
            description = "Get a todo by ID"
            parameters {
                parameter<Int>("id") { description = "The ID of the todo" }
            }
            execute { params -> todoRepository.getTodoById(params["id"]) }
        }

        tool("createTodo") {
            description = "Create a new todo"
            parameters {
                parameter<String>("name") { description = "The name of the todo" }
            }
            execute { params -> todoRepository.createTodo(params["name"]) }
        }
    }
}`}</CodeBlock>
                </TabItem>
              </Tabs>
            </section>
          </div>

          <div id="validate">
            <section className={styles.validateSection}>
              <h2>Validate</h2>
              <p>
                Specifications, such as those defined by Wirespec, play a
                critical role in validating the implementation of APIs by
                serving as the single source of truth. They ensure alignment
                between expected interfaces and actual code, facilitating robust
                development and testing practices.
              </p>

              <div className={styles.validateBlock}>
                <h3>Validation Through Specifications</h3>
                <p>
                  Specifications provide a blueprint for the implementation. By
                  describing the structure of data, endpoints, and channels in a
                  concise, human–readable manner, they form the foundation for
                  verifying that implementations align precisely with defined
                  interfaces. This is especially helpful for cross–functional
                  teams aiming to reduce ambiguity and miscommunication during
                  development.
                </p>
              </div>

              <div className={styles.validateBlock}>
                <h3>Generating Random Test Data</h3>
                <p>
                  Using specifications to generate randomized yet valid test
                  data ensures that input data conforms to expected formats and
                  constraints. Tools like Wirespec can leverage the detailed
                  schema definitions to create test scenarios that
                  systematically cover edge cases, identifying potential issues
                  early in the development cycle. This automated data generation
                  streamlines testing and improves reliability.
                </p>
              </div>

              <div className={styles.validateBlock}>
                <h3>Mock Servers for Testing</h3>
                <p>
                  Mock servers can be generated based on the same specification,
                  reducing dependency on live systems for testing. These servers
                  simulate the behavior of APIs as defined in the contracts,
                  allowing testing environments to mimic production without
                  requiring complete backend implementations. This approach
                  helps developers verify that their code interacts correctly
                  with the API’s interface, ensuring input/output consistency
                  and expected status codes are met.
                </p>
              </div>

              <div className={styles.validateBlock}>
                <h3>Detecting Discrepancies Early</h3>
                <p>
                  By using the specification to drive both code generation and
                  validation processes, inconsistencies can be detected before
                  they reach production. Automated tests generated directly from
                  the specification confirm that the code adheres to expected
                  behaviors and data formats defined in the agreed–upon
                  contract.
                </p>
              </div>

              <p>
                In summary, specifications like those enabled by Wirespec
                empower teams to validate implementations through code
                generation, automated testing, and mock servers, ensuring
                reliable and predictable API behavior.
              </p>
            </section>
          </div>
        </main>
      </div>
    </Layout>
  );
}
