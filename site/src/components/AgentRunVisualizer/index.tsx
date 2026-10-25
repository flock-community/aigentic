import { useEffect, useRef, useState, type ReactNode } from "react";
import clsx from "clsx";
import { AnimatePresence, motion, useInView, useReducedMotion } from "motion/react";
import CodeLines from "../CodeLines";
import CopyButton from "../CopyButton";
import styles from "./styles.module.css";

type Provider = {
  label: string;
  model: string;
  block: string[];
  imports: string[];
  usage: { input: number; output: number };
};

const PROVIDERS: Provider[] = [
  {
    label: "Gemini",
    model: "Gemini 3.5 Flash",
    block: [
      "    geminiModel {",
      '        apiKey("YOUR_API_KEY")',
      "        modelIdentifier(GeminiModelIdentifier.Gemini3_5Flash)",
      "    }",
    ],
    imports: [
      "community.flock.aigentic.gemini.dsl.geminiModel",
      "community.flock.aigentic.gemini.model.GeminiModelIdentifier",
    ],
    usage: { input: 412, output: 38 },
  },
  {
    label: "OpenAI",
    model: "GPT-5.5",
    block: [
      "    openAIModel {",
      '        apiKey("YOUR_API_KEY")',
      "        modelIdentifier(OpenAIModelIdentifier.GPT5_5)",
      "    }",
    ],
    imports: [
      "community.flock.aigentic.openai.dsl.openAIModel",
      "community.flock.aigentic.openai.model.OpenAIModelIdentifier",
    ],
    usage: { input: 398, output: 41 },
  },
  {
    label: "VertexAI",
    model: "Gemini 3.5 Flash on Vertex",
    block: [
      "    vertexAIModel {",
      '        project("YOUR_PROJECT_ID")',
      '        location("europe-west4")',
      "        modelIdentifier(VertexAIModelIdentifier.Gemini3_5Flash)",
      "    }",
    ],
    imports: [
      "community.flock.aigentic.vertexai.VertexAIModelIdentifier",
      "community.flock.aigentic.vertexai.dsl.vertexAIModel",
    ],
    usage: { input: 409, output: 37 },
  },
  {
    label: "Ollama",
    model: "Llama 3",
    block: [
      "    ollamaModel {",
      '        apiUrl("http://localhost:11434/v1/")',
      '        modelIdentifier(OpenAIModelIdentifier.Custom("llama3"))',
      "    }",
    ],
    imports: [
      "community.flock.aigentic.ollama.dsl.ollamaModel",
      "community.flock.aigentic.openai.model.OpenAIModelIdentifier",
    ],
    usage: { input: 455, output: 52 },
  },
];

const COMMON_IMPORTS = [
  "community.flock.aigentic.core.agent.start",
  "community.flock.aigentic.core.agent.tool.Outcome",
  "community.flock.aigentic.core.annotations.AigenticParameter",
  "community.flock.aigentic.core.dsl.agent",
];

const MODEL_START = 7;
const MAX_LINES = 28;
const QUESTION = "Weather in Amsterdam?";
const RESPONSE_FIELDS = ["celsius = 14.0", 'summary = "Light rain"'];
const FINAL_STEP = 4;
const STEP_DELAYS = [500, 1300, 1900, 1500];
const HOLD = 3200;

const snippetLines = (block: string[]) => [
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
  "        currentWeather(req.city)",
  "    }",
  "}",
  "",
  "suspend fun main() {",
  `    val run = weatherAgent.start("${QUESTION}")`,
  "    when (val outcome = run.outcome) {",
  "        is Outcome.Finished -> println(outcome.response)",
  "        is Outcome.Stuck -> println(outcome.reason)",
  "        is Outcome.Fatal -> println(outcome.message)",
  "    }",
  "}",
];

const range = (from: number, count: number) => Array.from({ length: count }, (_, i) => from + i);

const activeLinesFor = (step: number, lines: string[], blockLength: number) => {
  const lineOf = (needle: string) => lines.findIndex((line) => line.includes(needle));
  switch (step) {
    case 1:
      return [lineOf(".start(")];
    case 2:
      return range(MODEL_START, blockLength);
    case 3:
      return range(lineOf("addTool("), 3);
    case 4:
      return range(lineOf("when (val outcome"), 2);
    default:
      return [];
  }
};

const copyText = (provider: Provider, lines: string[]) =>
  [...COMMON_IMPORTS, ...provider.imports]
    .sort()
    .map((path) => `import ${path}`)
    .join("\n") +
  `\n\n${lines.join("\n")}\n\n// Replace with a call to a real weather API\nfun currentWeather(city: String) = "14.0°C, light rain in $city"\n`;

const useTypewriter = (text: string, active: boolean) => {
  const [count, setCount] = useState(text.length);
  useEffect(() => {
    if (!active) {
      setCount(text.length);
      return;
    }
    setCount(0);
    const timer = setInterval(() => setCount((current) => Math.min(current + 1, text.length)), 38);
    return () => clearInterval(timer);
  }, [active, text]);
  return text.slice(0, count);
};

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
  const ref = useRef<HTMLElement>(null);
  const inView = useInView(ref, { amount: 0.4 });
  const reduceMotion = useReducedMotion();
  const [step, setStep] = useState(FINAL_STEP);
  const [providerIndex, setProviderIndex] = useState(0);
  const [pinned, setPinned] = useState(false);
  const [playing, setPlaying] = useState(true);
  const [runCount, setRunCount] = useState(0);
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
          return;
        }
        if (!pinned) setProviderIndex((index) => (index + 1) % PROVIDERS.length);
        setRunCount((count) => count + 1);
        setStep(0);
      },
      step < FINAL_STEP ? STEP_DELAYS[step] : HOLD,
    );
    return () => clearTimeout(timer);
  }, [animating, step, pinned]);

  const choose = (index: number) => {
    setProviderIndex(index);
    setPinned(true);
    if (reduceMotion || !playing) {
      setStep(FINAL_STEP);
      return;
    }
    started.current = true;
    setRunCount((count) => count + 1);
    setStep(0);
  };

  const provider = PROVIDERS[providerIndex];
  const lines = snippetLines(provider.block);
  const typed = useTypewriter(QUESTION, animating && step === 1);
  const filling = animating && step === FINAL_STEP;

  return (
    <figure ref={ref} className={styles.visualizer} aria-label="Example agent run">
      <div className={styles.frame}>
        <div className={styles.toolbar}>
          <div className={styles.providers} role="group" aria-label="Model provider">
            <span className={styles.toolbarLabel}>Provider</span>
            {PROVIDERS.map(({ label }, index) => (
              <button
                key={label}
                type="button"
                className={clsx(styles.providerButton, index === providerIndex && styles.providerActive)}
                aria-pressed={index === providerIndex}
                onClick={() => choose(index)}
              >
                {label}
              </button>
            ))}
          </div>
          <div className={styles.actions}>
            <CopyButton text={copyText(provider, lines)} />
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
        </div>

        <div className={clsx(styles.pane, styles.codePane)}>
          <div className={styles.paneHeader}>
            <span className={styles.fileName}>WeatherAgent.kt</span>
          </div>
          <CodeLines
            code={lines.join("\n")}
            activeLines={activeLinesFor(step, lines, provider.block.length)}
            swap={{ key: providerIndex, lines: range(MODEL_START, provider.block.length), animate: !reduceMotion }}
            minLines={MAX_LINES}
            followActive
          />
        </div>

        <div className={clsx(styles.pane, styles.runPane)}>
          <div className={styles.paneHeader}>
            <span className={styles.fileName}>Run</span>
          </div>
          <ol className={styles.steps}>
            <Step index={1} step={step} label="Input">
              <p className={styles.bubble}>
                “{typed}
                {typed.length < QUESTION.length && <span className={styles.caret} aria-hidden="true" />}”
              </p>
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
                    {provider.model}
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
            <Step index={4} step={step} label={<code>Outcome.Finished</code>}>
              <code className={styles.response}>
                Weather(
                {RESPONSE_FIELDS.map((field, index) => (
                  <motion.span
                    key={`${runCount}-${filling}-${index}`}
                    initial={filling ? { opacity: 0 } : false}
                    animate={{ opacity: 1 }}
                    transition={{ duration: 0.3, delay: 0.3 + index * 0.4 }}
                  >
                    {field}
                    {index < RESPONSE_FIELDS.length - 1 && ", "}
                  </motion.span>
                ))}
                )
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
        Pick a provider or let it cycle. Each step lights up the code that drives it, and only the
        model block changes.
      </figcaption>
    </figure>
  );
}
