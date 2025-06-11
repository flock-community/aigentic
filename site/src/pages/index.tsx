import clsx from "clsx";
import Link from "@docusaurus/Link";
import Layout from "@theme/Layout";
import CodeBlock from "@theme/CodeBlock";
import Heading from "@theme/Heading";
import "../css/custom.css";
import styles from "./index.module.css";
import Tabs from "@theme/Tabs";
import TabItem from "@theme/TabItem";
import { JSX } from "react";

function HomepageHeader() {
  return (
    <header className={clsx("hero hero--primary", styles.heroBanner)}>
      <div className="container">
        <Heading
          as="h1"
          className={clsx("hero__title hero-heading", styles.heroHeading)}
        >
          Aigentic <br /> <span className="primary-text-color">AI Aagents with Kotlin</span>
        </Heading>
        <p className={clsx("hero__subtitle", styles.heroSubtitle)}>
          Streamline your LLM development journey with a powerful Kotlin DSL for building
          and integrating AI agents into applications
        </p>
        <div className={styles.buttons}>
          <Link
            className={clsx(styles.button, styles.buttonPrimary)}
            to="/docs/getting-started"
          >
            Get started
          </Link>
          <Link
            className={clsx(styles.button, styles.buttonDefault)}
            to="https://github.com/aigentic/aigentic"
          >
            GitHub
          </Link>
        </div>
      </div>
    </header>
  );
}

export default function Home(): JSX.Element {
  return (
    <Layout
      title="Aigentic: Kotlin DSL for AI Agents"
      description="Streamline your LLM development journey with a powerful Kotlin DSL for building and integrating AI agents into applications"
    >
      <HomepageHeader />
      <main className={clsx(styles.page)}>
        <section className="designFirstContent designFirstContent-blur pt-0">
          <div className="container">
            <div className="row row--align-center">
              <div className="col col--5">
                <div className="designFirstContent-left">
                  <Heading as="h2" className={clsx(styles.heading2)}>
                    <span className="primary-text-color">Intuitive</span> Kotlin{" "}
                    <br />
                    DSL{" "}
                  </Heading>
                  <p>
                    Building and integrating AI agents into applications can be complex and challenging.
                    Aigentic streamlines this process with a powerful Kotlin DSL that helps you:
                  </p>
                  <ul className={styles.listItemGroup}>
                    <li className={clsx(styles.listItem)}>
                      Rapidly create and deploy LLM agents
                    </li>
                    <li className={clsx(styles.listItem)}>
                      Seamlessly integrate with multiple LLM providers
                    </li>
                    <li className={clsx(styles.listItem)}>
                      Easily define and manage agent tools and capabilities
                    </li>
                    <li className={clsx(styles.listItem)}>
                      Efficiently handle message processing and context management
                    </li>
                    <li className={clsx(styles.listItem)}>
                      Transition smoothly from proof of concept to production
                    </li>
                    <li className={clsx(styles.listItem)}>
                      Iterate quickly based on real-world feedback
                    </li>
                  </ul>
                </div>
              </div>
              <div className="col col--7">
                <div className="card-nospace">
                  <img
                    src="/img/signal-2025-04-27-175547.png"
                    alt="aigentic-kotlin-dsl"
                  />
                </div>
              </div>
            </div>
          </div>
        </section>
        <section className="designFirstContent">
          <div className="container">
            <div className="row row--align-center">
              <div className="col col--7">
                <div className="card card-border-bottom card-nospace">
                  <img
                    src="/img/agent-code.png"
                    alt="aigentic-code-example"
                  />
                </div>
              </div>
              <div className="col col--5">
                <div className="designFirstContent-right">
                  <Heading as="h2" className={clsx(styles.heading2)}>
                    Why <span className="primary-text-color">Aigentic</span>
                  </Heading>
                  <p>
                    Aigentic is a Kotlin Multiplatform library that provides a powerful DSL for building
                    and integrating AI agents into applications. It streamlines the process of creating,
                    deploying, and managing LLM agents within your ecosystem.
                  </p>
                  <p>
                    By offering a model-agnostic approach, Aigentic supports multiple LLM providers
                    including OpenAI, Gemini, Ollama, and more. This flexibility allows you to choose
                    the best model for your specific use case without being locked into a single provider.
                  </p>
                  <p>
                    Additionally, Aigentic bridges the gap between proof of concept and production-ready
                    applications, enabling rapid iteration and improvement based on real-world feedback.
                    In short, Aigentic accelerates AI development, reduces complexity, and improves
                    integration with existing systems.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section className="steps">
          <div className="container">
            <div className="grid grid-cols-3">
              <div className="card card-border-left">
                <div className="icon-box">
                  <svg
                    width="28"
                    height="28"
                    viewBox="0 0 28 28"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      d="M18.6673 24.5L18.6673 22.1667C18.6673 20.929 18.1757 19.742 17.3005 18.8668C16.4253 17.9917 15.2383 17.5 14.0007 17.5L7.00065 17.5C5.76297 17.5 4.57599 17.9917 3.70082 18.8668C2.82565 19.742 2.33398 20.929 2.33398 22.1667L2.33398 24.5"
                      stroke="#101010"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <path
                      d="M10.5007 12.8333C13.078 12.8333 15.1673 10.744 15.1673 8.16667C15.1673 5.58934 13.078 3.5 10.5007 3.5C7.92332 3.5 5.83398 5.58934 5.83398 8.16667C5.83398 10.744 7.92332 12.8333 10.5007 12.8333Z"
                      stroke="#101010"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <path
                      d="M25.668 24.5007L25.668 22.1673C25.6672 21.1334 25.3231 20.1289 24.6896 19.3117C24.0561 18.4945 23.1691 17.9108 22.168 17.6523"
                      stroke="#101010"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <path
                      d="M18.668 3.65234C19.6718 3.90936 20.5615 4.49316 21.1969 5.31171C21.8322 6.13025 22.1771 7.13698 22.1771 8.17318C22.1771 9.20938 21.8322 10.2161 21.1969 11.0346C20.5615 11.8532 19.6718 12.437 18.668 12.694"
                      stroke="#101010"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                </div>
                <p>Create and deploy AI agents with intuitive Kotlin DSL</p>
              </div>
              <div className="card">
                <div className="icon-box">
                  <svg
                    width="28"
                    height="28"
                    viewBox="0 0 28 28"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      d="M10.5 3.5H5.83333C4.54467 3.5 3.5 4.54467 3.5 5.83333V10.5C3.5 11.7887 4.54467 12.8333 5.83333 12.8333H10.5C11.7887 12.8333 12.8333 11.7887 12.8333 10.5V5.83333C12.8333 4.54467 11.7887 3.5 10.5 3.5Z"
                      stroke="black"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <path
                      d="M8.16602 12.832V17.4987C8.16602 18.1175 8.41185 18.711 8.84943 19.1486C9.28702 19.5862 9.88051 19.832 10.4993 19.832H15.166"
                      stroke="black"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <path
                      d="M22.166 15.168H17.4993C16.2107 15.168 15.166 16.2126 15.166 17.5013V22.168C15.166 23.4566 16.2107 24.5013 17.4993 24.5013H22.166C23.4547 24.5013 24.4993 23.4566 24.4993 22.168V17.5013C24.4993 16.2126 23.4547 15.168 22.166 15.168Z"
                      stroke="black"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                </div>
                <p>Integrate with multiple LLM providers seamlessly</p>
              </div>
              <div className="card card-border-right">
                <div className="icon-box">
                  <svg
                    width="28"
                    height="28"
                    viewBox="0 0 28 28"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      d="M11.667 2.33203H16.3337"
                      stroke="black"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <path
                      d="M14 16.332L17.5 12.832"
                      stroke="black"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <path
                      d="M14.0003 25.6667C19.155 25.6667 23.3337 21.488 23.3337 16.3333C23.3337 11.1787 19.155 7 14.0003 7C8.84567 7 4.66699 11.1787 4.66699 16.3333C4.66699 21.488 8.84567 25.6667 14.0003 25.6667Z"
                      stroke="black"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                </div>
                <p>Accelerate from proof of concept to production</p>
              </div>
            </div>
          </div>
        </section>
        <section className="how">
          <div className="section-header">
            <div className="container">
              <Heading as="h2" className={clsx(styles.heading2)}>
                <span className="primary-text-color">How</span>
              </Heading>
            </div>
          </div>
          <div className="inner-section" id="how-design">
            <div className="container">
              <div className="row row--align-center">
                <div className="col col--6">
                  <div>
                    <Heading as="h2" className={clsx(styles.heading2)}>
                      Define
                    </Heading>
                    <p>
                      Aigentic provides an intuitive Kotlin DSL for defining AI agents with clear,
                      concise syntax. This approach allows you to specify agent behavior, tools,
                      and capabilities in a type-safe manner. The DSL abstracts away the complexity
                      of working with different LLM providers while giving you full control over
                      your agent's functionality.
                    </p>
                    <Link
                      className={clsx(
                        "button-link",
                        styles.button,
                        styles.buttonPrimary,
                      )}
                      to="/docs/getting-started"
                    >
                      Explore the DSL
                    </Link>
                  </div>
                </div>
                <div className="col col--6 code-block-col">
                  <div className="card card-border-bottom card-nospace">
                    <CodeBlock
                      language="kotlin"
                      title="SimpleAgent.kt"
                      className="custom-code-block"
                    >
                      {`val agent = agent {
    model = openAI {
        modelName = "gpt-4"
        apiKey = System.getenv("OPENAI_API_KEY")
    }

    tools {
        tool("calculator") {
            description = "Performs arithmetic calculations"
            parameters {
                parameter("expression", String) {
                    description = "The arithmetic expression to evaluate"
                }
            }
            handler { params ->
                val expression = params["expression"] as String
                val result = evaluateExpression(expression)
                result.toString()
            }
        }
    }

    messageHandler { messages ->
        // Process messages and generate responses
        processMessages(messages)
    }
}`}
                    </CodeBlock>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="inner-section" id="how-integrate">
            <div className="container">
              <div className="row row--align-center">
                <div className="col col--6 code-block-col">
                  <div>
                    <Tabs>
                      <TabItem value="OpenAI" label="OpenAI">
                        <CodeBlock language="kotlin">{`val model = openAI {
    modelName = "gpt-4"
    apiKey = System.getenv("OPENAI_API_KEY")
}`}</CodeBlock>
                      </TabItem>

                      <TabItem value="Gemini" label="Gemini">
                        <CodeBlock language="kotlin">{`val model = gemini {
    modelName = "gemini-pro"
    apiKey = System.getenv("GEMINI_API_KEY")
}`}</CodeBlock>
                      </TabItem>

                      <TabItem value="Ollama" label="Ollama">
                        <CodeBlock language="kotlin">{`val model = ollama {
    modelName = "llama2"
    baseUrl = "http://localhost:11434"
}`}</CodeBlock>
                      </TabItem>
                    </Tabs>
                  </div>
                </div>
                <div className="col col--6">
                  <div>
                    <Heading as="h2" className={clsx(styles.heading2)}>
                      Integrate
                    </Heading>
                    <p>
                      Aigentic is designed to be model-agnostic, allowing you to seamlessly
                      integrate with various LLM providers including OpenAI, Gemini, Ollama,
                      and more. This flexibility enables you to choose the best model for your
                      specific use case without being locked into a single provider. You can
                      even switch between providers with minimal code changes.
                    </p>
                    <Link
                      className={clsx(
                        "button-link",
                        styles.button,
                        styles.buttonPrimary,
                      )}
                      to="/docs/providers"
                    >
                      Explore Providers
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="inner-section" id="how-deploy">
            <div className="container">
              <div className="row row--align-center">
                <div className="col col--6">
                  <div className="how-content">
                    <Heading as="h2" className={clsx(styles.heading2)}>
                      Deploy
                    </Heading>
                    <p>
                      Aigentic bridges the gap between proof of concept and production-ready
                      applications. With its Kotlin Multiplatform foundation, you can deploy
                      your AI agents across different platforms and environments. Aigentic
                      provides tools for monitoring, logging, and managing your agents in
                      production, enabling you to iterate quickly based on real-world feedback.
                    </p>
                    <Link
                      className={clsx(
                        "button-link",
                        styles.button,
                        styles.buttonPrimary,
                      )}
                      to="/docs/deployment"
                    >
                      Deployment Guide
                    </Link>
                  </div>
                </div>
                <div className="col col--6 code-block-col">
                  <img src="/img/validate.png" alt="Deployment Diagram" />
                </div>
              </div>
            </div>
          </div>
        </section>
        <section className="other-capabilites other-capabilites-shadow ">
          <div className="container">
            <div className="row row--align-center">
              <div className="col col--6">
                <Heading as="h2" className={clsx(styles.heading2)}>
                  Key <span>capabilities</span>
                </Heading>
              </div>
              <div className="col col--6">
                <div className="card card-other card-border-bottom">
                  <Heading as="h4" className={clsx(styles.heading2)}>
                    LLM Providers
                  </Heading>
                  <p>
                    Aigentic supports multiple LLM providers, giving you flexibility and choice.
                  </p>
                  <p>
                    <span>Supported:</span> OpenAI, Gemini, Ollama, JSON Schema
                  </p>
                </div>
              </div>
              <div className="col col--6">
                <div className="card card-other card-border-bottom">
                  <Heading as="h4" className={clsx(styles.heading2)}>
                    Multiplatform
                  </Heading>
                  <p>Built with Kotlin Multiplatform for cross-platform compatibility.</p>
                  <p>
                    <span>Platforms:</span> JVM, JavaScript, Native
                  </p>
                </div>
              </div>
              <div className="col col--6">
                <div className="card card-other card-border-bottom">
                  <Heading as="h4" className={clsx(styles.heading2)}>
                    Tools
                  </Heading>
                  <p>Aigentic includes a variety of built-in tools for common agent tasks.</p>
                  <p>
                    <span>Included:</span> HTTP tools, OpenAPI tools, File operations, Data processing
                  </p>
                </div>
              </div>
              <div className="col col--6">
                <div className="card card-other card-border-bottom">
                  <Heading as="h4" className={clsx(styles.heading2)}>
                    Context Management
                  </Heading>
                  <p>
                    Efficient handling of conversation context and message history.
                  </p>
                  <p>
                    <span>Features:</span> Memory management, Context windowing, Summarization
                  </p>
                </div>
              </div>
              <div className="col col--6">
                <div className="card card-other card-border-bottom">
                  <Heading as="h4" className={clsx(styles.heading2)}>
                    Testing
                  </Heading>
                  <p>Comprehensive testing utilities for AI agent development.</p>
                  <p>
                    <span>Features:</span> Mock LLM responses, Test fixtures, Scenario testing
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section className="other-capabilites">
          <div className="container">
            <div className="row">
              <div className="col col--6">
                <Heading as="h2" className={clsx(styles.heading2)}>
                  Comparison with other <span>frameworks</span>
                </Heading>
                <p>
                  By understanding your project's specific needs and
                  requirements, you can choose the most suitable AI agent framework
                  to streamline development and enhance your applications.
                </p>
              </div>
            </div>
            <div className="table">
              <table>
                <thead>
                  <tr>
                    <th>Feature / Aspect</th>
                    <th>Aigentic</th>
                    <th>LangChain</th>
                    <th>LlamaIndex</th>
                    <th>AutoGPT</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>Primary focus</td>
                    <td>Streamlined API design</td>
                    <td>Restful API’s</td>
                    <td>Asynchronous API’s</td>
                    <td>Programmatic API design</td>
                  </tr>
                  <tr>
                    <td>Specification Format</td>
                    <td>Minimal, Wirespec syntax</td>
                    <td>YAML/JSON</td>
                    <td>YAML/JSON</td>
                    <td>TypeScript-like syntax</td>
                  </tr>
                  <tr>
                    <td>Ecosystem Support</td>
                    <td>Emerging</td>
                    <td>Mature and extensive</td>
                    <td>Growing rapidly</td>
                    <td>Emerging</td>
                  </tr>
                  <tr>
                    <td>Code Generation</td>
                    <td>Built-in, cross-language</td>
                    <td>Extensive via tools</td>
                    <td>Robust via tools</td>
                    <td>Flexible and customizable</td>
                  </tr>
                  <tr>
                    <td>Best for Microservices</td>
                    <td>Excellent</td>
                    <td>Good</td>
                    <td>Excellent</td>
                    <td>Good</td>
                  </tr>
                  <tr>
                    <td>Asynchronous Support</td>
                    <td>Limited</td>
                    <td>Limited</td>
                    <td>Excellent</td>
                    <td>Limited</td>
                  </tr>
                  <tr>
                    <td>Ease of Use</td>
                    <td>High (minimalist)</td>
                    <td>Moderate (can be verbose)</td>
                    <td>Moderate</td>
                    <td>Moderate (requires coding)</td>
                  </tr>
                  <tr>
                    <td>Technology</td>
                    <td>Multiplatform (JVM Node.js Binary)</td>
                    <td>JVM</td>
                    <td>Node.js</td>
                    <td>Node.js</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </section>
        <section className="other-capabilites">
          <div className="container">
            <div className="row row--align-center">
              <div className="col col--6">
                <Heading as="h2" className={clsx(styles.heading2)}>
                  Why <span>Aigentic</span>
                </Heading>
                <p>
                  Choosing Aigentic means selecting a powerful, flexible library designed
                  for developers who want to build production-ready AI applications with
                  Kotlin's type safety and expressiveness.
                </p>
                <p>
                  Our commitment to ongoing innovation and quality ensures that
                  Aigentic not only meets current demands but also evolves with
                  emerging AI technologies and best practices.
                </p>
                <p>
                  By actively engaging in the open-source community and
                  maintaining transparent development processes, we ensure that
                  Aigentic remains at the forefront of AI agent development. Our focus on
                  developer experience, performance, and reliability provides you with a
                  framework that elevates your AI projects to new heights.
                </p>
              </div>
              <div className="col col--1"></div>
              <div className="col col--5">
                <div className="card-flock-wrap">
                  <div className="card card-border-bottom card-nospace card-flock">
                    <img
                      src="/img/hero-image.png"
                      alt="Aigentic AI Agents"
                      width="100%"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </Layout>
  );
}
