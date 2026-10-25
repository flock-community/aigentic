import Link from "@docusaurus/Link";
import CopyButton from "../CopyButton";
import { dependencyLine, useAigenticVersion } from "../InstallSnippet";
import styles from "./styles.module.css";

type Module = { name: string; text: string };
type Group = { title: string; modules: Module[]; link: { label: string; to: string } };

const GROUPS: Group[] = [
  {
    title: "Start here",
    modules: [{ name: "core", text: "Agents, tools, workflows and the typed DSL. Runs on the JVM and Node.js." }],
    link: { label: "Agents docs", to: "/docs/dsl/agent" },
  },
  {
    title: "Pick a provider",
    modules: [
      { name: "openai", text: "OpenAI models." },
      { name: "gemini", text: "Google Gemini models." },
      { name: "vertexai", text: "Gemini on Google Cloud VertexAI. JVM only." },
      { name: "ollama", text: "Local models through Ollama." },
    ],
    link: { label: "Providers docs", to: "/docs/dsl/providers" },
  },
  {
    title: "Connect your systems",
    modules: [{ name: "openapi", text: "Turn an OpenAPI spec into tools the agent can call." }],
    link: { label: "Integrations docs", to: "/docs/dsl/integrations" },
  },
  {
    title: "Run in production",
    modules: [
      { name: "platform", text: "Send runs to the Aigentic platform and replay them as regression tests." },
      { name: "google-cloud-function", text: "Deploy an agent as a Google Cloud Function. Node.js only." },
    ],
    link: {
      label: "Testing example",
      to: "https://github.com/flock-community/aigentic/tree/main/src/examples/testing",
    },
  },
];

export default function ModuleMap() {
  const version = useAigenticVersion();
  return (
    <div className={styles.map}>
      {GROUPS.map((group, index) => (
        <section key={group.title} className={styles.group}>
          <header className={styles.groupHeader}>
            <span className={styles.groupNumber}>{String(index + 1).padStart(2, "0")}</span>
            <h3>{group.title}</h3>
          </header>
          <ul className={styles.modules}>
            {group.modules.map((module) => (
              <li key={module.name} className={styles.module}>
                <div className={styles.moduleHeader}>
                  <code>{module.name}</code>
                  <CopyButton compact label="Copy dependency" text={dependencyLine(module.name, version)} />
                </div>
                <p>{module.text}</p>
              </li>
            ))}
          </ul>
          <Link className={styles.groupLink} to={group.link.to}>
            {group.link.label}
          </Link>
        </section>
      ))}
    </div>
  );
}
