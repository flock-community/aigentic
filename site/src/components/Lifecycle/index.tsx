import clsx from "clsx";
import Link from "@docusaurus/Link";
import styles from "./styles.module.css";

type Lane = { chip: string; to: string; text: string };

type Phase = {
  name: string;
  question: string;
  code: Lane;
  platform: Lane;
};

const PHASES: Phase[] = [
  {
    name: "Prototype",
    question: "Does it work at all?",
    code: { chip: "agent<I, O> { }", to: "/docs/dsl/agent", text: "Describe the task and try it on a few examples." },
    platform: { chip: "Runs", to: "/docs/platform/runs#runs", text: "Connect early and see every run you try." },
  },
  {
    name: "Golden set",
    question: "What's the right answer?",
    code: {
      chip: "Expected(…)",
      to: "/docs/platform/ground-truth#up-front-when-you-start-the-run",
      text: "Send the answers you already know with each run.",
    },
    platform: {
      chip: "Evaluations",
      to: "/docs/platform/evaluations",
      text: "Confirm the right value per field, once per input.",
    },
  },
  {
    name: "Validate",
    question: "Is it good enough to ship?",
    code: {
      chip: 'task("…")',
      to: "/docs/dsl/agent#defining-tasks",
      text: "Change the task or model until the numbers are there.",
    },
    platform: {
      chip: "Evaluation result",
      to: "/docs/platform/evaluations#read-the-results",
      text: "Accuracy per field and every error, for this version.",
    },
  },
  {
    name: "Go live",
    question: "Is it still right in production?",
    code: {
      chip: "addToEvaluationSet",
      to: "/docs/platform/ground-truth#later-when-someone-has-checked-the-result",
      text: "Corrections from your own process become ground truth.",
    },
    platform: {
      chip: "Runs and costs",
      to: "/docs/platform/runs#costs",
      text: "Every run with its cost and version, scored as people correct it.",
    },
  },
  {
    name: "Improve",
    question: "Did the change make it better?",
    code: {
      chip: "geminiModel { }",
      to: "/docs/dsl/providers",
      text: "Apply the proposal, or try another model. Its runs become the next version.",
    },
    platform: {
      chip: "Fix assistant",
      to: "/docs/platform/fix-assistant",
      text: "Proposes a change. Compare versions field by field.",
    },
  },
];

const LoopIcon = () => (
  <svg viewBox="0 0 16 16" aria-hidden="true" focusable="false">
    <path
      d="M13 8a5 5 0 1 1-1.5-3.6M13 2.5v2.5h-2.5"
      fill="none"
      strokeWidth="1.4"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

export default function Lifecycle({ className }: { className?: string }) {
  return (
    <figure className={clsx(styles.figure, className)}>
      <div className={styles.lanes}>
        <span className={styles.frame} aria-hidden="true" />
        <span className={clsx(styles.laneLabel, styles.codeLabel)} aria-hidden="true">
          In your code
        </span>
        <span className={clsx(styles.laneLabel, styles.platformLabel)} aria-hidden="true">
          On the platform
        </span>
        <ol className={styles.phases} aria-label="The agent lifecycle">
          {PHASES.map((phase, index) => (
            <li key={phase.name} className={styles.phase}>
              <div className={styles.head}>
                <span className={styles.number}>{index + 1}</span>
                <strong className={styles.name}>{phase.name}</strong>
                <span className={styles.question}>{phase.question}</span>
              </div>
              <div className={clsx(styles.cell, styles.code)}>
                <span className={styles.cellLabel}>In your code</span>
                <Link className={clsx(styles.chip, styles.codeChip)} to={phase.code.to}>
                  <code>{phase.code.chip}</code>
                </Link>
                <p>{phase.code.text}</p>
              </div>
              <div className={clsx(styles.cell, styles.platform)}>
                <span className={styles.cellLabel}>On the platform</span>
                <Link className={clsx(styles.chip, styles.place)} to={phase.platform.to}>
                  {phase.platform.chip}
                </Link>
                <p>{phase.platform.text}</p>
              </div>
              {index === PHASES.length - 1 && (
                <span className={styles.loopNote} aria-hidden="true">
                  <LoopIcon />
                  Then back to 3, Validate
                </span>
              )}
            </li>
          ))}
        </ol>
        <span className={styles.loop} aria-hidden="true" />
      </div>
      <figcaption className={styles.caption}>
        <LoopIcon />
        <span>
          <strong>Improve</strong> leads back to <strong>Validate</strong>: every new version is scored against the same
          golden set.
        </span>
      </figcaption>
    </figure>
  );
}
