import { useEffect, useRef, useState, type ReactNode } from "react";
import clsx from "clsx";
import { AnimatePresence, motion, useInView, useReducedMotion } from "motion/react";
import { Highlight } from "prism-react-renderer";
import { usePrismTheme } from "@docusaurus/theme-common";
import styles from "./styles.module.css";

type Provider = {
  name: string;
  block: string[];
  usage: { input: number; output: number };
};

const PROVIDERS: Provider[] = [
  {
    name: "Gemini 3.8 Flash",
    block: [
      "    geminiModel {",
      '        apiKey("YOUR_API_KEY")',
      "        modelIdentifier(GeminiModelIdentifier.Gemini3_8Flash)",
      "    }",
    ],
    usage: { input: 412, output: 38 },
  },
  {
    name: "GPT-5.5",
    block: [
      "    openAIModel {",
      '        apiKey("YOUR_API_KEY")',
      "        modelIdentifier(OpenAIModelIdentifier.GPT5_5)",
      "    }",
    ],
    usage: { input: 398, output: 41 },
  },
  {
    name: "Llama 3 on Ollama",
    block: [
      "    ollamaModel {",
      '        apiUrl("http://localhost:11434/v1/")',
      '        modelIdentifier(OpenAIModelIdentifier.Custom("llama3"))',
      "    }",
    ],
    usage: { input: 455, output: 52 },
  },
];

const snippet = (block: string[]) =>
  [
    "@AigenticParameter",
    "data class Weather(val celsius: Double, val summary: String)",
    "",
    "@AigenticParameter",
    "data class WeatherRequest(val city: String)",
    "",
    "val weatherAgent = agent<String, Weather> {",
    ...block,
    '    task("Answer questions about the weather") {',
    '        addInstruction("Use getWeather for live data")',
    "    }",
    '    addTool("getWeather") { req: WeatherRequest ->',
    "        weatherApi.current(req.city)",
    "    }",
    "}",
    "",
    'val run = weatherAgent.start("Weather in Amsterdam?")',
    "",
    "when (val outcome = run.outcome) {",
    "    is Outcome.Finished -> println(outcome.response)",
    "    is Outcome.Stuck -> println(outcome.reason)",
    "    is Outcome.Fatal -> println(outcome.message)",
    "}",
  ].join("\n");

const MODEL_LINES = [7, 8, 9, 10];
const ACTIVE_LINES: Record<number, number[]> = {
  1: [19],
  2: MODEL_LINES,
  3: [14, 15, 16],
  4: [21, 22],
};
const FINAL_STEP = 4;
const STEP_DELAYS = [500, 1300, 1900, 1500];
const HOLD = 3200;

const stepState = (index: number, step: number) =>
  step > index ? "done" : step === index ? "current" : "pending";

type StepProps = {
  index: number;
  step: number;
  label: ReactNode;
  children: ReactNode;
};

const Step = ({ index, step, label, children }: StepProps) => (
  <li className={styles.step} data-state={stepState(index, step)}>
    <span className={styles.node} aria-hidden="true" />
    <span className={styles.stepLabel}>{label}</span>
    <div className={styles.stepBody}>{children}</div>
  </li>
);

const Thinking = () => (
  <span className={styles.thinking}>
    <span className={styles.srOnly}>thinking</span>
    <span aria-hidden="true" />
    <span aria-hidden="true" />
    <span aria-hidden="true" />
  </span>
);

export default function AgentRunVisualizer() {
  const prismTheme = usePrismTheme();
  const ref = useRef<HTMLElement>(null);
  const inView = useInView(ref, { amount: 0.4 });
  const reduceMotion = useReducedMotion();
  const [step, setStep] = useState(FINAL_STEP);
  const [providerIndex, setProviderIndex] = useState(0);
  const [playing, setPlaying] = useState(true);
  const started = useRef(false);
  const animating = playing && inView && !reduceMotion;

  useEffect(() => {
    if (!animating) return;
    if (!started.current) {
      started.current = true;
      setStep(0);
      return;
    }
    const timer = setTimeout(
      () => {
        if (step < FINAL_STEP) {
          setStep(step + 1);
        } else {
          setProviderIndex((index) => (index + 1) % PROVIDERS.length);
          setStep(0);
        }
      },
      step < FINAL_STEP ? STEP_DELAYS[step] : HOLD,
    );
    return () => clearTimeout(timer);
  }, [animating, step]);

  const provider = PROVIDERS[providerIndex];
  const activeLines = ACTIVE_LINES[step] ?? [];

  return (
    <figure ref={ref} className={styles.visualizer} aria-label="Example agent run">
      <div className={styles.frame}>
        <div className={styles.pane}>
          <div className={styles.paneHeader}>
            <span className={styles.fileName}>WeatherAgent.kt</span>
          </div>
          <Highlight theme={prismTheme} code={snippet(provider.block)} language="kotlin">
            {({ tokens, getLineProps, getTokenProps }) => (
              <pre className={styles.code}>
                <code>
                  {tokens.map((line, lineIndex) => {
                    const content = line.map((token, key) => (
                      <span key={key} {...getTokenProps({ token })} />
                    ));
                    const { className, ...lineProps } = getLineProps({ line });
                    return (
                      <div
                        key={lineIndex}
                        {...lineProps}
                        className={clsx(
                          className,
                          styles.line,
                          activeLines.includes(lineIndex) && styles.lineActive,
                        )}
                      >
                        <span className={styles.lineNumber} aria-hidden="true">
                          {lineIndex + 1}
                        </span>
                        {MODEL_LINES.includes(lineIndex) ? (
                          <motion.span
                            key={`${providerIndex}-${lineIndex}`}
                            className={styles.lineContent}
                            initial={animating ? { opacity: 0, y: 6 } : false}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ duration: 0.35, delay: (lineIndex - MODEL_LINES[0]) * 0.05 }}
                          >
                            {content}
                          </motion.span>
                        ) : (
                          <span className={styles.lineContent}>{content}</span>
                        )}
                      </div>
                    );
                  })}
                </code>
              </pre>
            )}
          </Highlight>
        </div>

        <div className={clsx(styles.pane, styles.runPane)}>
          <div className={styles.paneHeader}>
            <span className={styles.fileName}>Run</span>
            {!reduceMotion && (
              <button
                type="button"
                className={styles.toggle}
                onClick={() => setPlaying(!playing)}
                aria-label={playing ? "Pause animation" : "Play animation"}
              >
                {playing ? (
                  <svg viewBox="0 0 16 16" aria-hidden="true">
                    <rect x="4" y="3" width="3" height="10" rx="1" />
                    <rect x="9" y="3" width="3" height="10" rx="1" />
                  </svg>
                ) : (
                  <svg viewBox="0 0 16 16" aria-hidden="true">
                    <path d="M5 3.5v9a.5.5 0 0 0 .77.42l7-4.5a.5.5 0 0 0 0-.84l-7-4.5A.5.5 0 0 0 5 3.5Z" />
                  </svg>
                )}
              </button>
            )}
          </div>
          <ol className={styles.steps}>
            <Step index={1} step={step} label="Input">
              <p className={styles.bubble}>“Weather in Amsterdam?”</p>
            </Step>
            <Step index={2} step={step} label="Model">
              <div className={styles.modelRow}>
                <AnimatePresence mode="wait" initial={false}>
                  <motion.span
                    key={providerIndex}
                    className={styles.chip}
                    initial={{ opacity: 0, y: 6 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -6 }}
                    transition={{ duration: 0.25 }}
                  >
                    {provider.name}
                  </motion.span>
                </AnimatePresence>
                {step === 2 ? (
                  <Thinking />
                ) : (
                  <span className={styles.note}>
                    calls <code>getWeather</code>
                  </span>
                )}
              </div>
            </Step>
            <Step index={3} step={step} label="Tool call">
              <code className={styles.call}>getWeather(city = "Amsterdam")</code>
              <code className={styles.result}>"14.0°C, light rain in Amsterdam"</code>
            </Step>
            <Step
              index={4}
              step={step}
              label={<code>Outcome.Finished</code>}
            >
              <code className={styles.response}>
                Weather(celsius = 14.0, summary = "Light rain")
              </code>
            </Step>
          </ol>
          <p className={styles.usage} data-visible={step === FINAL_STEP}>
            <code>run.tokenUsage()</code>
            <span>
              {provider.usage.input} input, {provider.usage.output} output tokens
            </span>
          </p>
        </div>
      </div>
      <figcaption className={styles.caption}>
        Each step lights up the code that drives it. Every run swaps the model block to show that
        switching providers changes nothing else.
      </figcaption>
    </figure>
  );
}
