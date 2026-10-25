import clsx from "clsx";
import Link from "@docusaurus/Link";
import Layout from "@theme/Layout";
import Heading from "@theme/Heading";
import "../css/custom.css";
import styles from "./index.module.css";
import { JSX } from "react";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import CyclingBlurText from "../components/CyclingBlurText";
import AgentRunVisualizer from "../components/AgentRunVisualizer";
import CopyButton from "../components/CopyButton";
import ProductionStory from "../components/ProductionStory";

const dslPoints = [
  {
    token: "agent<I, O>",
    text: "Typed input in, typed output out. Your data classes tell the model what to return.",
  },
  {
    token: "addTool",
    text: "Tools are plain Kotlin lambdas. The agent decides when to call them.",
  },
  {
    token: "run.outcome",
    text: "Every run ends as Finished, Stuck or Fatal, so errors come back as values.",
  },
];

const capabilities = [
  {
    title: "LLM providers",
    text: "OpenAI, Gemini, VertexAI and Ollama, or your own Model implementation.",
    to: "/docs/dsl/providers",
  },
  {
    title: "Kotlin Multiplatform",
    text: "Run your agents on the JVM or on JavaScript with Node.js.",
    to: "/docs/getting-started",
  },
  {
    title: "Tools and integrations",
    text: "Kotlin lambdas as tools, OpenAPI specs as tools, and Google Cloud Functions.",
    to: "/docs/dsl/tools",
  },
  {
    title: "Documents and images",
    text: "Attach images on any provider and PDFs on Gemini and VertexAI, plus fixed context like text and URLs.",
    to: "/docs/dsl/agent",
  },
  {
    title: "Testing",
    text: "Replay recorded runs as regression tests, with tool calls mocked from the recording.",
    to: "https://github.com/flock-community/aigentic/tree/main/src/examples/testing",
  },
  {
    title: "Workflows",
    text: "Chain up to five agents, with types checked at every step.",
    to: "/docs/workflow",
  },
];

function InstallSnippet() {
  const { siteConfig } = useDocusaurusContext();
  const version = siteConfig.customFields?.version as string;
  const dependencies = ["core", "gemini"]
    .map((module) => `implementation("community.flock.aigentic:${module}:${version}")`)
    .join("\n");
  return (
    <div className={styles.install}>
      <div className={styles.installCode}>
        <pre>
          <code>{dependencies}</code>
        </pre>
        <CopyButton text={dependencies} />
      </div>
      <p>
        Latest release v{version}.{" "}
        <Link to={`https://github.com/flock-community/aigentic/releases/tag/v${version}`}>
          Read the release notes
        </Link>
      </p>
    </div>
  );
}

function HomepageHeader() {
  return (
    <header className={clsx("hero hero--primary", styles.heroBanner)}>
      <div className="container">
        <div className={clsx("hero__title hero-heading", styles.heroHeading)}>
          <CyclingBlurText
            phrases={['production-ready', 'reliable', 'LLM-agnostic']}
            prefix="Aigentic helps you build"
            suffix="AI agents faster"
          />
        </div>
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
            to="https://github.com/flock-community/aigentic"
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
            <div className={clsx("row", styles.dslIntro)}>
              <div className="col col--6">
                <Heading as="h2" className={clsx(styles.heading2)}>
                  <span className="primary-text-color">Intuitive</span> Kotlin DSL
                </Heading>
                <p className={styles.lead}>
                  Describe the task, give the agent tools, and get a Kotlin data class back.
                  Here's a complete agent and what happens when it runs.
                </p>
              </div>
              <div className="col col--6">
                <ul className={styles.points}>
                  {dslPoints.map(({ token, text }) => (
                    <li key={token}>
                      <code>{token}</code>
                      <span>{text}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
            <AgentRunVisualizer />
            <div className={styles.centeredAction}>
              <Link className={clsx(styles.button, styles.buttonPrimary)} to="/docs/getting-started">
                Build your first agent
              </Link>
              <InstallSnippet />
            </div>
          </div>
        </section>
        <section className="how">
          <div className={clsx("container", styles.sectionIntro)}>
            <Heading as="h2" className={clsx(styles.heading2)}>
              From first agent to <span>production</span>
            </Heading>
            <p>Swap models, read documents, chain agents and test the results.</p>
          </div>
          <div className="container">
            <ProductionStory />
          </div>
        </section>
        <section className="other-capabilites other-capabilites-shadow">
          <div className="container">
            <Heading as="h2" className={clsx(styles.heading2)}>
              Key <span>capabilities</span>
            </Heading>
            <div className={styles.capabilities}>
              {capabilities.map(({ title, text, to }) => (
                <Link key={title} className={styles.capability} to={to}>
                  <Heading as="h3">{title}</Heading>
                  <p>{text}</p>
                </Link>
              ))}
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
                  Getting an agent demo running is easy. Getting it into production, next to the rest
                  of your code, is the hard part. Aigentic is built for that step.
                </p>
                <p>
                  Agents are typed, so the compiler checks your inputs, outputs and tools. They're
                  testable, so you can change a prompt without guessing. And they read like any other
                  Kotlin code, so your team can review them.
                </p>
                <p>
                  Aigentic is open source and built by Flock. Watch the talk for a walkthrough, or use
                  the Initializr to start a project with the right dependencies.
                </p>
              </div>
              <div className="col col--6">
                <div className={styles.video}>
                  <iframe
                    src="https://www.youtube.com/embed/MitNJPjN_SQ?si=W2GDiyoaVmTdWX7M"
                    title="Aigentic talk on YouTube"
                    loading="lazy"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                    referrerPolicy="strict-origin-when-cross-origin"
                    allowFullScreen
                  ></iframe>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </Layout>
  );
}
