import clsx from "clsx";
import Link from "@docusaurus/Link";
import Layout from "@theme/Layout";
import Heading from "@theme/Heading";
import styles from "./index.module.css";
import { JSX, type ReactNode, useRef } from "react";
import CyclingBlurText from "../components/CyclingBlurText";
import AgentRunVisualizer from "../components/AgentRunVisualizer";
import BeforeAfter from "../components/BeforeAfter";
import ProductionStory from "../components/ProductionStory";
import FlockMark from "../components/FlockMark";
import StartSection from "../components/StartSection";
import FlockHero from "../components/FlockHero";
import Lifecycle from "../components/Lifecycle";
import useBaseUrl from "@docusaurus/useBaseUrl";

const platformPoints = [
  {
    title: "Every run",
    text: "recorded with its input, tool calls, cost and config version.",
  },
  {
    title: "Accuracy per field",
    text: "against answers people confirmed, or that your process already has.",
  },
  {
    title: "A fix assistant",
    text: "that tries changes on your recorded inputs and proposes the best one, with its cost per run.",
  },
];

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

function SectionTitle({ children, centered = false }: { children: ReactNode; centered?: boolean }) {
  return (
    <Heading as="h2" className={clsx(styles.heading2, styles.sectionTitle, centered && styles.sectionTitleCentered)}>
      <FlockMark />
      {children}
    </Heading>
  );
}

function HomepageHeader() {
  const contentRef = useRef<HTMLDivElement>(null);
  return (
    <header className={clsx("hero hero--primary", styles.heroBanner)}>
      <FlockHero contentRef={contentRef} />
      <div ref={contentRef} className={clsx("container", styles.heroContent)}>
        <h1 className={clsx("hero__title hero-heading", styles.heroHeading)}>
          <CyclingBlurText
            phrases={['production-ready', 'reliable', 'LLM-agnostic']}
            prefix="Aigentic helps you build"
            suffix="AI agents faster"
          />
        </h1>
        <p className={clsx("hero__subtitle", styles.heroSubtitle)}>
          A Kotlin DSL for typed AI agents that work with any model, and a platform that shows how often they're right
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

function PlatformSection() {
  const screenshot = useBaseUrl("/img/platform/fix-assistant-result.png");
  return (
    <section className={clsx("how", styles.platformSection)}>
      <div className={clsx("container", styles.sectionIntro)}>
        <SectionTitle centered>Know how often it's right</SectionTitle>
        <p>
          A demo shows your agent can get it right. The Aigentic Platform shows how often it does, field by field, and
          whether your last change made it better or worse.
        </p>
      </div>
      <div className="container">
        <Lifecycle className={styles.lifecycle} />
        <div className={clsx("row row--align-center", styles.platformProof)}>
          <div className="col col--5">
            <ul className={styles.proofPoints}>
              {platformPoints.map(({ title, text }) => (
                <li key={title}>
                  <strong>{title}</strong> {text}
                </li>
              ))}
            </ul>
            <p className={styles.hosting}>Hosted by Flock in the EU, or in your own environment.</p>
            <div className={styles.proofActions}>
              <Link className={clsx(styles.button, styles.buttonPrimary)} to="/docs/platform">
                Explore the platform
              </Link>
              <Link className={clsx(styles.button, styles.buttonDefault)} to="/docs/lifecycle">
                Read the guide
              </Link>
            </div>
          </div>
          <div className="col col--7">
            <img
              className={styles.shot}
              src={screenshot}
              alt="A fix assistant result on the Aigentic Platform: $.invoiceNumber goes from 70% to 90% correct, $.total stays at 80%, a run costs $0.0062 instead of $0.0039, with the fixed and regressed inputs per field."
              width={1300}
              height={824}
              loading="lazy"
              decoding="async"
            />
          </div>
        </div>
      </div>
    </section>
  );
}

export default function Home(): JSX.Element {
  return (
    <Layout
      title="Aigentic: Kotlin DSL for AI Agents"
      description="Build typed AI agents in Kotlin, then measure how often they're right and fix what isn't on the Aigentic Platform."
    >
      <HomepageHeader />
      <main className={clsx(styles.page)}>
        <section className="designFirstContent designFirstContent-blur pt-0">
          <div className="container">
            <div className={clsx("row", styles.dslIntro)}>
              <div className="col col--6">
                <SectionTitle>Intuitive Kotlin DSL</SectionTitle>
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
            </div>
          </div>
        </section>
        <section className="other-capabilites other-capabilites-shadow">
          <div className="container">
            <SectionTitle>Why Aigentic</SectionTitle>
            <p className={styles.lead}>
              Most agent libraries help you build the demo. Aigentic is built for what comes after: typed agents in
              your existing Kotlin code, any provider, and a platform that measures how often they're right. Here's
              the same weather agent written both ways.
            </p>
            <BeforeAfter />
            <div className={clsx("row row--align-center", styles.talk)}>
              <div className="col col--5">
                <Heading as="h3">Built by Flock, in the open</Heading>
                <p>
                  Aigentic is open source. Watch the talk for a walkthrough, or start from the Initializr to get a
                  project with the right dependencies.
                </p>
              </div>
              <div className="col col--7">
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
        <section className="how">
          <div className={clsx("container", styles.sectionIntro)}>
            <SectionTitle centered>From first agent to production</SectionTitle>
            <p>Connect your APIs, read documents, chain agents and test the results.</p>
          </div>
          <div className="container">
            <ProductionStory />
          </div>
        </section>
        <PlatformSection />
        <StartSection showModules />
      </main>
    </Layout>
  );
}
