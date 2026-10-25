import { useEffect, useRef, useState, type ReactNode } from "react";
import clsx from "clsx";
import Link from "@docusaurus/Link";
import Heading from "@theme/Heading";
import { AnimatePresence, motion, useReducedMotion } from "motion/react";
import CodeLines from "../CodeLines";
import {
  invoiceExtractorExample,
  openApiExample,
  regressionTestExample,
  workflowExample,
} from "../../code/examples";
import styles from "./styles.module.css";

type StoryStep = {
  title: string;
  body: ReactNode;
  link: { label: string; to: string };
  file: string;
  code: string;
  highlight: string[];
};

const STEPS: StoryStep[] = [
  {
    title: "Tools from your API",
    body: (
      <p>
        Already have an API? Point <code>openApiTools</code> at its OpenAPI spec and every operation becomes a tool
        the agent can call. Add authentication headers once, for every call.
      </p>
    ),
    link: { label: "Read about integrations", to: "/docs/dsl/integrations" },
    file: "OrderAgent.kt",
    code: openApiExample,
    highlight: ["openApiTools(", "addHeader("],
  },
  {
    title: "Structured data from documents",
    body: (
      <p>
        Pass a PDF or an image to <code>start()</code> and get a typed data class back. Use{" "}
        <code>@Description</code> to tell the model what a field means. There's no JSON to parse and no tool to
        write. PDFs work with Gemini and VertexAI today.
      </p>
    ),
    link: { label: "Read about attachments", to: "/docs/dsl/agent#running-an-agent-with-attachments" },
    file: "InvoiceExtractor.kt",
    code: invoiceExtractorExample,
    highlight: ["@Description", "Attachment.Base64.pdf", "Outcome.Finished"],
  },
  {
    title: "Workflows",
    body: (
      <p>
        Chain agents with <code>thenProcess</code>. The output type of one agent is the input type of the next, so
        the compiler catches a broken chain before you run it. Each agent can use its own model.
      </p>
    ),
    link: { label: "Explore workflows", to: "/docs/workflow" },
    file: "DocumentWorkflow.kt",
    code: workflowExample,
    highlight: ["agent<Document, Analysis>", "agent<Analysis, Summary>", "thenProcess"],
  },
  {
    title: "Test before you ship",
    body: (
      <p>
        Tag good runs on the Aigentic platform, then replay them as regression tests whenever you change a prompt or
        a model. Tool calls return the recorded results, so tests never touch your real systems. Flock hosts the
        platform and gives access on request.
      </p>
    ),
    link: {
      label: "See the testing example",
      to: "https://github.com/flock-community/aigentic/tree/main/src/examples/testing",
    },
    file: "WeatherAgentTest.kt",
    code: regressionTestExample,
    highlight: ["regressionTest {", "agent(weatherAgent)", "tags(", "numberOfIterations(", "}.start()", "check("],
  },
];

const highlightedLines = ({ code, highlight }: StoryStep) =>
  code
    .split("\n")
    .flatMap((line, index) => (highlight.some((needle) => line.includes(needle)) ? [index] : []));

export default function ProductionStory() {
  const [active, setActive] = useState(0);
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
  const transition = { duration: reduceMotion ? 0 : 0.3 };

  return (
    <div className={styles.story}>
      <div className={styles.steps}>
        {STEPS.map((storyStep, index) => (
          <article
            key={storyStep.title}
            ref={(element) => {
              stepRefs.current[index] = element;
            }}
            data-index={index}
            className={clsx(styles.step, index === active && styles.stepActive)}
          >
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
              <CodeLines code={storyStep.code} activeLines={highlightedLines(storyStep)} />
            </div>
          </article>
        ))}
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
          </div>
          <div className={styles.panelBody}>
            <AnimatePresence mode="wait" initial={false}>
              <motion.div
                key={active}
                initial={{ opacity: 0, y: reduceMotion ? 0 : 12 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: reduceMotion ? 0 : -12 }}
                transition={transition}
              >
                <CodeLines code={step.code} activeLines={highlightedLines(step)} />
              </motion.div>
            </AnimatePresence>
          </div>
        </div>
      </div>
    </div>
  );
}
