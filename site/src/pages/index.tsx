import clsx from "clsx";
import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import Layout from "@theme/Layout";
import CodeBlock from "@theme/CodeBlock";
import HomepageFeatures from "@site/src/components/HomepageFeatures";
import Heading from "@theme/Heading";
import "../css/custom.css";
import styles from "./index.module.css";
import Tabs from "@theme/Tabs";
import TabItem from "@theme/TabItem";

function HomepageHeader() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <header className={clsx("hero hero--primary", styles.heroBanner)}>
      <div className="container">
        <Heading
          as="h1"
          className={clsx("hero__title hero-heading", styles.heroHeading)}
        >
          Aigentic <br />
          <span className="primary-text-color">AI Agents with Kotlin</span>
        </Heading>
        <p className={clsx("hero__subtitle", styles.heroSubtitle)}>
          A Kotlin Multiplatform library for building and integrating AI agents into your applications
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
            to="https://playground.aigentic.io/"
          >
            Playground
          </Link>
        </div>
      </div>
    </header>
  );
}

export default function Home(): JSX.Element {
  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout
      title="Aigentic - AI Agents with Kotlin"
      description="A Kotlin Multiplatform library for building and integrating AI agents into your applications"
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
                    DSL
                  </Heading>
                  <p>
                    Building AI agents can be complex and time-consuming. Aigentic simplifies this process with a clean,
                    intuitive Kotlin DSL that addresses common challenges:
                  </p>
                  <ul className={styles.listItemGroup}>
                    <li className={clsx(styles.listItem)}>
                      Complex integration with different LLM providers
                    </li>
                    <li className={clsx(styles.listItem)}>
                      Difficulty in creating and managing tools for agents
                    </li>
                    <li className={clsx(styles.listItem)}>
                      Lack of type safety in agent definitions
                    </li>
                    <li className={clsx(styles.listItem)}>
                      Boilerplate code for handling conversations
                    </li>
                    <li className={clsx(styles.listItem)}>
                      Limited multiplatform support
                    </li>
                    <li className={clsx(styles.listItem)}>
                      Steep learning curve for AI integration
                    </li>
                  </ul>
                </div>
              </div>
              <div className="col col--7">
                <div className=" card-nospace">
                  <img
                    src="/img/intuitive-kotlin-dsl.png"
                    alt="intuitive-kotlin-dsl"
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
                    src="/img/aigentic-features.jpg"
                    alt="aigentic-features"
                  />
                </div>
              </div>
              <div className="col col--5">
                <div className="designFirstContent-right">
                  <Heading as="h2" className={clsx(styles.heading2)}>
                    Why <span className="primary-text-color">Aigentic</span>
                  </Heading>
                  <p>
                    Aigentic is a Kotlin Multiplatform library that provides a powerful DSL for building and integrating
                    AI agents into your applications. It streamlines the process of creating, deploying, and managing
                    LLM agents within your software ecosystem.
                  </p>
                  <p>
                    By providing a model-agnostic approach, Aigentic allows you to easily switch between different LLM
                    providers without changing your application logic. This flexibility enables you to choose the right
                    model for your specific use case.
                  </p>
                  <p>
                    With a rich set of built-in tools and the ability to create custom tools, Aigentic empowers your
                    agents to perform complex tasks and interact with external systems. The type-safe schema system
                    ensures data integrity and provides clear documentation.
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
                <p>Create AI agents with an intuitive Kotlin DSL</p>
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
                <p>Connect to multiple LLM providers seamlessly</p>
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
                <p>Build powerful tools for your AI agents</p>
              </div>
            </div>
          </div>
        </section>
        <section className="how">
          <div className="section-header">
            <div className="container">
              <Heading as="h2" className={clsx(styles.heading2)}>
                <span className="primary-text-color">Features</span>
              </Heading>
            </div>
          </div>
          <div className="inner-section" id="kotlin-dsl">
            <div className="container">
              <div className="row row--align-center">
                <div className="col col--6">
                  <div>
                    <Heading as="h2" className={clsx(styles.heading2)}>
                      Kotlin DSL
                    </Heading>
                    <p>
                      Aigentic provides a powerful and intuitive Kotlin DSL for building AI agents. The DSL is designed
                      to be expressive, type-safe, and easy to use, allowing developers to create complex AI agents with
                      minimal boilerplate code. With the Aigentic DSL, you can define agents, tools, and schemas in a
                      natural, readable way.
                    </p>
                    <Link
                      className={clsx(
                        "button-link",
                        styles.button,
                        styles.buttonPrimary
                      )}
                      to="/docs/language"
                    >
                      Explore Kotlin DSL
                    </Link>
                  </div>
                </div>
                <div className="col col--6 code-block-col">
                  <div className="card card-border-bottom card-nospace">
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
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="inner-section" id="providers">
            <div className="container">
              <div className="row row--align-center">
                <div className="col col--6 code-block-col">
                  <div>
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
                  </div>
                </div>
                <div className="col col--6">
                  <div>
                    <Heading as="h2" className={clsx(styles.heading2)}>
                      LLM Providers
                    </Heading>
                    <p>
                      Aigentic is designed to be model-agnostic, allowing you to easily switch between different LLM
                      providers without changing your application logic. This flexibility enables you to choose the
                      right model for your specific use case, balancing factors like cost, performance, and capabilities.
                    </p>
                    <Link
                      className={clsx(
                        "button-link",
                        styles.button,
                        styles.buttonPrimary
                      )}
                      to="/docs/language/providers"
                    >
                      Explore Providers
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="inner-section" id="tools">
            <div className="container">
              <div className="row row--align-center">
                <div className="col col--6">
                  <div className="how-content">
                    <Heading as="h2" className={clsx(styles.heading2)}>
                      Tools System
                    </Heading>
                    <p>
                      Tools are a core concept in Aigentic that extend an agent's capabilities beyond just conversation.
                      They allow agents to perform specific actions, access external systems, and process data in
                      structured ways. Aigentic provides a flexible and powerful way to define tools using the Kotlin DSL,
                      and comes with several built-in tools that you can use out of the box.
                    </p>
                    <Link
                      className={clsx(
                        "button-link",
                        styles.button,
                        styles.buttonPrimary
                      )}
                      to="/docs/language/tools"
                    >
                      Explore Tools
                    </Link>
                  </div>
                </div>
                <div className="col col--6 code-block-col">
                  <img src="/img/validate.png" alt="Tools System" />
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
                    Multiplatform
                  </Heading>
                  <p>
                    Aigentic is built with Kotlin Multiplatform, allowing you to use it on multiple platforms.
                  </p>
                  <p>
                    <span>Platforms:</span> JVM, JavaScript, Native
                  </p>
                </div>
              </div>
              <div className="col col--6">
                <div className="card card-other card-border-bottom">
                  <Heading as="h4" className={clsx(styles.heading2)}>
                    Type Safety
                  </Heading>
                  <p>Leverage Kotlin's strong type system for safe agent definitions.</p>
                  <p>
                    <span>Features:</span> Type-safe builders, schema validation, compile-time checks
                  </p>
                </div>
              </div>
              <div className="col col--6">
                <div className="card card-other card-border-bottom">
                  <Heading as="h4" className={clsx(styles.heading2)}>
                    Providers
                  </Heading>
                  <p>Connect to various LLM providers with a consistent interface.</p>
                  <p>
                    <span>Supported:</span> OpenAI, Gemini, Ollama, and more
                  </p>
                </div>
              </div>
              <div className="col col--6">
                <div className="card card-other card-border-bottom">
                  <Heading as="h4" className={clsx(styles.heading2)}>
                    Built-in Tools
                  </Heading>
                  <p>
                    Aigentic comes with several built-in tools for common tasks.
                  </p>
                  <p>
                    <span>Tools:</span> HTTP, OpenAPI, File System, and more
                  </p>
                </div>
              </div>
              <div className="col col--6">
                <div className="card card-other card-border-bottom">
                  <Heading as="h4" className={clsx(styles.heading2)}>
                    Extensibility
                  </Heading>
                  <p>Easily extend Aigentic with custom tools and providers.</p>
                  <p>
                    <span>Features:</span> Custom tools, custom providers, schema extensions
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
                    <td>Kotlin DSL for AI agents</td>
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
                  Why Aigentic from <span>Flock</span>
                </Heading>
                <p>
                  Choosing Aigentic means selecting a product developed and supported by Flock., a community of
                  passionate and driven professionals dedicated to continuous improvement.
                </p>
                <p>
                  Our dedication to ongoing innovation and quality ensures that
                  Aigentic not only meets current demands but also evolves with
                  emerging technologies.
                </p>
                <p>
                  By actively engaging in the open-source community and
                  maintaining transparent development processes, we ensure that
                  continuity and advancement are well-anchored. At Flock., we
                  combine deep technical expertise with a strong focus on
                  collaboration and knowledge sharing, providing you with a
                  partner who elevates your projects to new heights.
                </p>
              </div>
              <div className="col col--1"></div>
              <div className="col col--5">
                <div className="card-flock-wrap">
                  <div className="card card-border-bottom card-nospace card-flock">
                    <iframe
                      width="560"
                      height="315"
                      src="https://www.youtube.com/embed/LMA5ByWUhBo?si=4BK1w7GIIVfr2doG"
                      title="YouTube video player"
                      frameBorder="0"
                      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                      referrerPolicy="strict-origin-when-cross-origin"
                      allowFullScreen
                    ></iframe>
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
