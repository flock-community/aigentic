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
    text: "Each run goes to the Aigentic Platform with its messages, tool calls, cost and config version.",
    chip: "platform { }",
    area: "record",
  },
  {
    title: "Evaluate",
    text: "Confirm the right values once, on the platform or from your code. See how often each output field is right, per version.",
    chip: "Evaluations",
    area: "evaluate",
  },
  {
    title: "Fix",
    text: "The fix assistant tries changes on your recorded inputs and proposes the one that scores best, with its cost per run.",
    chip: "Fix assistant",
    area: "fix",
  },
  {
    title: "Improve",
    text: "Apply the change in your code. Its runs are scored against the same ground truth, so you know it's better before you ship. Then the loop starts again.",
    chip: "back to Build",
    area: "improve",
  },
];

const Arrow = ({ area, direction }: { area: string; direction: "right" | "left" | "down" | "up" }) => (
  <li className={clsx(styles.arrowItem, styles[area])} aria-hidden="true">
    <svg className={clsx(styles.arrow, styles[direction])} viewBox="0 0 48 16">
      <path d="M2 8h42M38 2l7 6-7 6" fill="none" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  </li>
);

export default function VisionPage() {
  return (
    <Layout
      title="Vision"
      description="Aigentic is built around a loop: build an agent, run it, record what it did, measure how often it's right, fix what's wrong and improve with confidence."
    >
      <main className={styles.visionMain}>
        <section className={styles.visionSection}>
          <h1 className={styles.title}>The Aigentic vision</h1>
          <p className={styles.lead}>
            A demo agent is easy to build. A production agent needs more: a way to see what it did, know how often it
            gets the answer right, and improve it without guessing. Aigentic is built around that loop.
          </p>

          <ol className={styles.loop} aria-label="The Aigentic loop">
            {STEPS.map((step, index) => (
              <li key={step.title} className={clsx(styles.step, styles[step.area])}>
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
              Build and Run need only the core library and a provider. Record, Evaluate and Fix use the{" "}
              <code>platform</code> module and the Aigentic Platform, hosted by Flock or in your own environment.{" "}
              <Link to="/docs/platform">Read the platform docs</Link>, or see how tagged runs become{" "}
              <Link to="/docs/platform/regression-tests">regression tests</Link>.
            </p>
          </div>
        </section>
      </main>
    </Layout>
  );
}
