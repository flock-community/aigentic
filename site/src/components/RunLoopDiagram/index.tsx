import PixelMark from "../PixelMark";
import styles from "./styles.module.css";

const Arrow = () => (
  <svg className={styles.arrow} viewBox="0 0 48 16" aria-hidden="true">
    <path d="M0 8h42M36 2l7 6-7 6" fill="none" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

export function RunLoopDiagram() {
  return (
    <figure className={styles.diagram} aria-label="How an agent run works">
      <div className={styles.box}>
        <span className={styles.label}>Input</span>
        <code>I</code>
        <p>A String, a data class, attachments, or nothing at all.</p>
      </div>
      <Arrow />
      <div className={`${styles.box} ${styles.agent}`}>
        <span className={styles.label}>
          <PixelMark />
          <code>agent&lt;I, O&gt;</code>
        </span>
        <dl className={styles.parts}>
          <dt>Task</dt>
          <dd>What the agent should do, plus instructions</dd>
          <dt>Model</dt>
          <dd>Any provider: OpenAI, Gemini, VertexAI, Ollama</dd>
          <dt>Tools</dt>
          <dd>Your Kotlin code the model can call</dd>
        </dl>
        <p className={styles.loop}>
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <path d="M13 8a5 5 0 1 1-1.5-3.6M13 2.5v2.5h-2.5" fill="none" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
          The model calls tools until it can finish the task
        </p>
      </div>
      <Arrow />
      <div className={styles.box}>
        <span className={styles.label}>
          <code>run.outcome</code>
        </span>
        <ul className={styles.outcomes}>
          <li>
            <code>Finished(response: O)</code>
            <span>Done, with your typed result</span>
          </li>
          <li>
            <code>Stuck(reason)</code>
            <span>The model couldn't finish</span>
          </li>
          <li>
            <code>Fatal(message)</code>
            <span>Something failed, no exception thrown</span>
          </li>
        </ul>
      </div>
    </figure>
  );
}

export function WorkflowDiagram() {
  return (
    <figure className={styles.workflow} aria-label="How a workflow chains agents">
      <code className={styles.type}>Document</code>
      <Arrow />
      <code className={styles.step}>agent&lt;Document, Analysis&gt;</code>
      <Arrow />
      <code className={styles.type}>Analysis</code>
      <Arrow />
      <code className={styles.step}>agent&lt;Analysis, Summary&gt;</code>
      <Arrow />
      <code className={styles.type}>Summary</code>
    </figure>
  );
}
