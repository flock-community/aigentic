import clsx from "clsx";
import Link from "@docusaurus/Link";
import Layout from "@theme/Layout";
import Heading from "@theme/Heading";
import styles from "./index.module.css";
import { JSX, type ReactNode } from "react";
import CyclingBlurText from "../components/CyclingBlurText";
import AgentRunVisualizer from "../components/AgentRunVisualizer";
import ModuleMap from "../components/ModuleMap";
import BeforeAfter from "../components/BeforeAfter";
import ProductionStory from "../components/ProductionStory";
import PixelMark from "../components/PixelMark";

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
      <PixelMark />
      {children}
    </Heading>
  );
}

function HomepageHeader() {
  return (
    <header className={clsx("hero hero--primary", styles.heroBanner)}>
      <div className="container">
        <h1 className={clsx("hero__title hero-heading", styles.heroHeading)}>
          <CyclingBlurText
            phrases={['production-ready', 'reliable', 'LLM-agnostic']}
            prefix="Aigentic helps you build"
            suffix="AI agents faster"
          />
        </h1>
        <p className={clsx("hero__subtitle", styles.heroSubtitle)}>
          A Kotlin DSL for AI agents that are typed, testable and work with any model
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
      description="A Kotlin DSL for AI agents that are typed, testable and work with any model"
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
        <section className="how">
          <div className={clsx("container", styles.sectionIntro)}>
            <SectionTitle centered>From first agent to production</SectionTitle>
            <p>Connect your APIs, read documents, chain agents and test the results.</p>
          </div>
          <div className="container">
            <ProductionStory />
          </div>
        </section>
        <section className="other-capabilites other-capabilites-shadow">
          <div className="container">
            <SectionTitle>Pick the modules you need</SectionTitle>
            <p className={styles.lead}>
              Everything is on Maven Central under <code>community.flock.aigentic</code>. Start with core, one
              provider and a Ktor engine such as <code>ktor-client-cio</code>, then add what your agent needs.
            </p>
            <ModuleMap />
          </div>
        </section>
        <section className="other-capabilites">
          <div className="container">
            <SectionTitle>Why Aigentic</SectionTitle>
            <p className={styles.lead}>
              Getting an agent demo running is easy. Getting it into production, next to the rest of your code, is
              the hard part. Here's the same weather agent written both ways.
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
      </main>
    </Layout>
  );
}
