import clsx from "clsx";
import Link from "@docusaurus/Link";
import Layout from "@theme/Layout";
import Heading from "@theme/Heading";
import styles from "./index.module.css";
import { JSX, type ReactNode, useRef } from "react";
import CyclingBlurText from "../components/CyclingBlurText";
import AgentRunVisualizer from "../components/AgentRunVisualizer";
import AigenticPicture from "../components/AigenticPicture";
import ProductionStory from "../components/ProductionStory";
import FlockMark from "../components/FlockMark";
import StartSection from "../components/StartSection";
import FlockHero from "../components/FlockHero";
import VersionComparison from "../components/VersionComparison";

const ACCESS_MAIL = "mailto:aigentic@flock.community?subject=Aigentic%20Platform%20access";

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
        <div className={clsx("row row--align-center", styles.platformProof)}>
          <div className={clsx("col col--5", styles.proofText)}>
            <Heading as="h3">Did the change make it better?</Heading>
            <p>
              Every run your agent publishes is recorded with its config version. Confirm the right answers once, and
              each new version is scored on the same inputs: which fields got better, which got worse, and what a run
              costs.
            </p>
            <p className={styles.hosting}>
              Hosted by Flock in the EU, or set up in your own environment.
            </p>
            <div className={styles.proofActions}>
              <Link className={clsx(styles.button, styles.buttonPrimary)} to={ACCESS_MAIL}>
                Request access
              </Link>
              <Link className={clsx(styles.button, styles.buttonDefault)} to="/docs/lifecycle">
                See how it works
              </Link>
            </div>
            <p className={styles.accessNote}>
              Request access opens an email to <span className={styles.address}>aigentic@flock.community</span>. Tell
              us what your agent does and where you'd like the platform to run, and we'll help you connect your first
              agent.
            </p>
          </div>
          <div className="col col--7">
            <VersionComparison />
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
      description="Build typed AI agents in Kotlin. The Aigentic Platform measures how often they're right and helps you fix what isn't."
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
              how the two work together, from your first agent to its next version.
            </p>
            <AigenticPicture />
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
        <PlatformSection />
        <section className="how">
          <div className={clsx("container", styles.sectionIntro)}>
            <SectionTitle centered>What a real agent needs</SectionTitle>
            <p>Connect your APIs, read documents, chain agents and measure the results.</p>
          </div>
          <div className="container">
            <ProductionStory />
          </div>
        </section>
        <StartSection showModules />
      </main>
    </Layout>
  );
}
