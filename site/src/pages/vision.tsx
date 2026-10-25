import React from "react";
import clsx from "clsx";
import Layout from "@theme/Layout";
import Link from "@docusaurus/Link";
import styles from "./vision.module.css";

type LoopStep = {
  title: string;
  text: string;
  chip: string;
  area: string;
};

const STEPS: LoopStep[] = [
  {
    title: "Build",
    text: "Define the agent in Kotlin: typed input and output, a task, tools and a model.",
    chip: "agent<I, O> { }",
    area: "build",
  },
  {
    title: "Run",
    text: "Run it inside your application on the JVM or Node.js, with the provider you choose.",
    chip: "agent.start(input)",
    area: "run",
  },
  {
    title: "Record",
    text: "Each run goes to the Aigentic platform with its messages, tool calls and token usage.",
    chip: "platform { }",
    area: "record",
  },
  {
    title: "Tag",
    text: "Review runs on the platform and tag the ones that went right.",
    chip: "Aigentic platform",
    area: "tag",
  },
  {
    title: "Replay",
    text: "Replay tagged runs as regression tests. Tool calls return the recorded results, so nothing touches your real systems.",
    chip: "regressionTest { }",
    area: "replay",
  },
  {
    title: "Improve",
    text: "Change a prompt, a tool or the model, and ship once the tests pass. Then the loop starts again.",
    chip: "back to Build",
    area: "improve",
  },
];

const Arrow = ({ area, direction }: { area: string; direction: "right" | "left" | "down" | "up" }) => (
  <li className={styles.arrowItem} style={{ gridArea: area }} aria-hidden="true">
    <svg className={clsx(styles.arrow, styles[direction])} viewBox="0 0 48 16">
      <path d="M2 8h42M38 2l7 6-7 6" fill="none" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  </li>
);

export default function VisionPage() {
  return (
    <Layout
      title="Vision"
      description="Aigentic is built around a loop: build an agent, run it, record what it did, replay good runs as tests and improve with confidence."
    >
      <main className={styles.visionMain}>
        <section className={styles.visionSection}>
          <h1 className={styles.title}>The Aigentic vision</h1>
          <p className={styles.lead}>
            A demo agent is easy to build. A production agent needs more: a way to see what it did, check that it still
            works after a change, and improve it without guessing. Aigentic is built around that loop.
          </p>

          <ol className={styles.loop} aria-label="The Aigentic loop">
            {STEPS.map((step, index) => (
              <li key={step.title} className={styles.step} style={{ gridArea: step.area }}>
                <span className={styles.number}>{String(index + 1).padStart(2, "0")}</span>
                <h2>{step.title}</h2>
                <p>{step.text}</p>
                <code>{step.chip}</code>
              </li>
            ))}
            <Arrow area="a1" direction="right" />
            <Arrow area="a2" direction="right" />
            <Arrow area="a3" direction="down" />
            <Arrow area="a4" direction="left" />
            <Arrow area="a5" direction="left" />
            <Arrow area="a6" direction="up" />
          </ol>

          <div className={styles.today}>
            <p>
              Build and Run need only the core library and a provider. Record, Tag and Replay use the{" "}
              <code>platform</code> module and the hosted Aigentic platform.
            </p>
            <div className={styles.actions}>
              <Link className={styles.primary} to="/docs/getting-started">
                Build your first agent
              </Link>
              <Link
                className={styles.secondary}
                to="https://github.com/flock-community/aigentic/tree/main/src/examples/testing"
              >
                See the testing example
              </Link>
            </div>
          </div>
        </section>
      </main>
    </Layout>
  );
}
