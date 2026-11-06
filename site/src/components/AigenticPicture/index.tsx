import clsx from "clsx";
import Link from "@docusaurus/Link";
import { motion } from "motion/react";
import useHydratedReducedMotion from "@site/src/hooks/useHydratedReducedMotion";
import styles from "./styles.module.css";

type Zone = "library" | "platform";

type Step = {
  name: string;
  text: string;
  chip: string;
  to: string;
  zone: Zone;
  code?: boolean;
};

const ZONES: Record<Zone, { title: string; detail: string }> = {
  library: { title: "Aigentic library", detail: "In your code · open source" },
  platform: { title: "Aigentic Platform", detail: "Hosted by Flock or in your environment" },
};

const STEPS: Step[] = [
  {
    name: "Build",
    text: "Describe a typed agent: its task, tools and any model.",
    chip: "agent<I, O> { }",
    to: "/docs/dsl/agent",
    zone: "library",
    code: true,
  },
  {
    name: "Run",
    text: "It runs inside your app, with your own model keys.",
    chip: "agent.start(input)",
    to: "/docs/getting-started",
    zone: "library",
    code: true,
  },
  {
    name: "Record",
    text: "Every run arrives with its cost and config version.",
    chip: "Runs",
    to: "/docs/platform/runs",
    zone: "platform",
  },
  {
    name: "Evaluate",
    text: "Accuracy per field, against answers you confirmed.",
    chip: "Evaluations",
    to: "/docs/platform/evaluations",
    zone: "platform",
  },
  {
    name: "Fix",
    text: "An assistant proposes the change that scores best.",
    chip: "Fix assistant",
    to: "/docs/platform/fix-assistant",
    zone: "platform",
  },
];

const Arrow = ({ className }: { className?: string }) => (
  <svg className={className} viewBox="0 0 32 12" aria-hidden="true" focusable="false">
    <path d="M1 6h28M24 1.5 29.5 6 24 10.5" fill="none" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

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

const isZoneStart = (index: number) => index === 0 || STEPS[index - 1].zone !== STEPS[index].zone;
const isZoneEnd = (index: number) => index === STEPS.length - 1 || STEPS[index + 1].zone !== STEPS[index].zone;

export default function AigenticPicture() {
  const reduceMotion = useHydratedReducedMotion();
  /* Always animate to visible: with reduced motion the transition is instant, so the SSR state never sticks. */
  const reveal = (index: number) => ({
    initial: { opacity: 0, y: 10 },
    whileInView: { opacity: 1, y: 0 },
    viewport: { once: true, amount: 0.3 },
    transition: reduceMotion
      ? { duration: 0 }
      : { duration: 0.5, delay: index * 0.12, ease: [0.2, 0.7, 0.2, 1] as const },
  });

  return (
    <figure className={styles.figure}>
      <div className={styles.picture}>
        <span className={clsx(styles.zone, styles.libraryZone)} aria-hidden="true" />
        <span className={clsx(styles.zone, styles.platformZone)} aria-hidden="true" />
        {(Object.keys(ZONES) as Zone[]).map((zone) => (
          <p key={zone} className={clsx(styles.zoneLabel, styles[`${zone}Label`])} aria-hidden="true">
            <strong>{ZONES[zone].title}</strong>
            <span>{ZONES[zone].detail}</span>
          </p>
        ))}
        <ol className={styles.steps} aria-label="Aigentic from building an agent to its next version">
          {STEPS.map((step, index) => (
            <motion.li
              key={step.name}
              className={clsx(
                styles.step,
                styles[step.zone],
                isZoneStart(index) && styles.zoneStart,
                isZoneEnd(index) && styles.zoneEnd,
              )}
              {...reveal(index)}
            >
              {isZoneStart(index) && (
                <span className={styles.zoneTag}>
                  <strong>{ZONES[step.zone].title}</strong> {ZONES[step.zone].detail}
                </span>
              )}
              <span className={styles.name}>
                <span className={styles.number}>{index + 1}</span>
                {step.name}
              </span>
              <span className={styles.text}>{step.text}</span>
              <Link className={clsx(styles.chip, step.code ? styles.codeChip : styles.place)} to={step.to}>
                {step.code ? <code>{step.chip}</code> : step.chip}
              </Link>
              {index < STEPS.length - 1 && (
                <span className={clsx(styles.next, isZoneEnd(index) && styles.handoff)} aria-hidden="true">
                  <Arrow />
                  {isZoneEnd(index) && <span>every run</span>}
                </span>
              )}
            </motion.li>
          ))}
        </ol>
        <motion.span
          className={styles.loop}
          aria-hidden="true"
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={{ once: true, amount: 0.3 }}
          transition={reduceMotion ? { duration: 0 } : { duration: 0.6, delay: STEPS.length * 0.12 }}
        />
      </div>
      <figcaption className={styles.caption}>
        <LoopIcon />
        <span>
          <strong>Improve:</strong> apply the change in your code, and the next version is scored on the same inputs.
        </span>
      </figcaption>
    </figure>
  );
}
