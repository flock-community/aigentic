import { useEffect, useRef, useState, type ReactNode } from "react";
import clsx from "clsx";
import Link from "@docusaurus/Link";
import Heading from "@theme/Heading";
import { AnimatePresence, motion, useReducedMotion } from "motion/react";
import CodeLines from "../CodeLines";
import {
  invoiceExtractorExample,
  providerExamples,
  regressionTestExample,
  workflowExample,
} from "../../code/examples";
import styles from "./styles.module.css";

type StoryStep = {
  title: string;
  body: ReactNode;
  link: { label: string; to: string };
  file: string;
  code: (providerIndex: number) => string;
  highlight: string[];
};

const STEPS: StoryStep[] = [
  {
    title: "Providers",
    body: (
      <p>
        Aigentic works with OpenAI, Gemini, VertexAI and Ollama, and you can plug in your own model.
        Switching providers means changing one block. Your task, tools and types stay the same.
      </p>
    ),
    link: { label: "Explore providers", to: "/docs/dsl/providers" },
    file: "Agent.kt",
    code: (providerIndex) => providerExamples[providerIndex].code,
    highlight: [],
  },
  {
    title: "Structured data from documents",
    body: (
      <p>
        Pass a PDF or an image to <code>start()</code> and get a typed data class back. Use{" "}
        <code>@Description</code> to tell the model what a field means. There's no JSON to parse and
        no tool to write. PDFs work with Gemini and VertexAI today.
      </p>
    ),
    link: { label: "Read about agents", to: "/docs/dsl/agent" },
    file: "InvoiceExtractor.kt",
    code: () => invoiceExtractorExample,
    highlight: ["@Description", "Attachment.Base64.pdf", "Outcome.Finished"],
  },
  {
    title: "Workflows",
    body: (
      <p>
        Chain agents with <code>thenProcess</code>. The output type of one agent is the input type of
        the next, so the compiler catches a broken chain before you run it. Each agent can use its own
        model.
      </p>
    ),
    link: { label: "Explore workflows", to: "/docs/workflow" },
    file: "DocumentWorkflow.kt",
    code: () => workflowExample,
    highlight: ["agent<Document, Analysis>", "agent<Analysis, Summary>", "thenProcess"],
  },
  {
    title: "Test before you ship",
    body: (
      <>
        <p>
          Tag good runs on the Aigentic platform, then replay them as regression tests whenever you
          change a prompt or a model. Tool calls return the recorded results, so tests never touch your
          real systems.
        </p>
        <p>
          A failed model call doesn't throw. It comes back as <code>Outcome.Fatal</code>, and{" "}
          <code>run.tokenUsage()</code> tells you what each run cost.
        </p>
      </>
    ),
    link: {
      label: "See the testing example",
      to: "https://github.com/flock-community/aigentic/tree/main/src/examples/testing",
    },
    file: "WeatherAgentTest.kt",
    code: () => regressionTestExample,
    highlight: ["regressionTest {", "agent(weatherAgent)", "tags(", "numberOfIterations(", "}.start()", "check("],
  },
];

const highlightedLines = (code: string, needles: string[]) =>
  code
    .split("\n")
    .flatMap((line, index) => (needles.some((needle) => line.includes(needle)) ? [index] : []));

const providerHighlight = (code: string) => {
  const lines = code.split("\n");
  const end = lines.findIndex((line, index) => index > 0 && line === "    }");
  return Array.from({ length: end }, (_, i) => i + 1);
};

const linesFor = (stepIndex: number, code: string) =>
  stepIndex === 0 ? providerHighlight(code) : highlightedLines(code, STEPS[stepIndex].highlight);

export default function ProductionStory() {
  const [active, setActive] = useState(0);
  const [providerIndex, setProviderIndex] = useState(0);
  const stepRefs = useRef<(HTMLElement | null)[]>([]);
  const reduceMotion = useReducedMotion();

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) =>
        entries
          .filter((entry) => entry.isIntersecting)
          .forEach((entry) => setActive(Number((entry.target as HTMLElement).dataset.index))),
      { rootMargin: "-45% 0px -45% 0px" },
    );
    stepRefs.current.forEach((element) => element && observer.observe(element));
    return () => observer.disconnect();
  }, []);

  const step = STEPS[active];
  const code = step.code(providerIndex);
  const transition = { duration: reduceMotion ? 0 : 0.3 };

  return (
    <div className={styles.story}>
      <div className={styles.steps}>
        {STEPS.map((storyStep, index) => {
          const stepCode = storyStep.code(providerIndex);
          return (
            <article
              key={storyStep.title}
              ref={(element) => {
                stepRefs.current[index] = element;
              }}
              data-index={index}
              className={clsx(styles.step, index === active && styles.stepActive)}
            >
              <span className={styles.stepNumber}>
                {String(index + 1).padStart(2, "0")} / {String(STEPS.length).padStart(2, "0")}
              </span>
              <Heading as="h3" className={styles.stepTitle}>
                {storyStep.title}
              </Heading>
              {storyStep.body}
              <Link className={styles.stepLink} to={storyStep.link.to}>
                {storyStep.link.label}
              </Link>
              <div className={clsx(styles.frame, styles.inlineCode)}>
                <div className={styles.panelHeader}>
                  <span className={styles.fileName}>{storyStep.file}</span>
                </div>
                <CodeLines code={stepCode} activeLines={linesFor(index, stepCode)} />
              </div>
            </article>
          );
        })}
      </div>
      <div className={styles.panel} role="region" aria-label="Code for the current step">
        <div className={styles.frame}>
          <div className={styles.panelHeader}>
            <AnimatePresence mode="wait" initial={false}>
              <motion.span
                key={step.file}
                className={styles.fileName}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={transition}
              >
                {step.file}
              </motion.span>
            </AnimatePresence>
            {active === 0 && (
              <div className={styles.tabs} role="group" aria-label="Provider">
                {providerExamples.map(({ label }, index) => (
                  <button
                    key={label}
                    type="button"
                    aria-pressed={index === providerIndex}
                    className={clsx(styles.tab, index === providerIndex && styles.tabActive)}
                    onClick={() => setProviderIndex(index)}
                  >
                    {label}
                  </button>
                ))}
              </div>
            )}
          </div>
          <div className={styles.panelBody}>
            <AnimatePresence mode="wait" initial={false}>
              <motion.div
                key={`${active}-${active === 0 ? providerIndex : 0}`}
                initial={{ opacity: 0, y: reduceMotion ? 0 : 12 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: reduceMotion ? 0 : -12 }}
                transition={transition}
              >
                <CodeLines code={code} activeLines={linesFor(active, code)} />
              </motion.div>
            </AnimatePresence>
          </div>
        </div>
      </div>
    </div>
  );
}
