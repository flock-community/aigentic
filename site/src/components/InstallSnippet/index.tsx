import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import CopyButton from "../CopyButton";
import styles from "./styles.module.css";

export const useAigenticVersion = () => useDocusaurusContext().siteConfig.customFields?.version as string;

const useKtorVersion = () => useDocusaurusContext().siteConfig.customFields?.ktorVersion as string;

export const dependencyLine = (module: string, version: string) =>
  `implementation("community.flock.aigentic:${module}:${version}")`;

export default function InstallSnippet() {
  const version = useAigenticVersion();
  const ktorVersion = useKtorVersion();
  const dependencies = [
    ...["core", "gemini"].map((module) => dependencyLine(module, version)),
    `implementation("io.ktor:ktor-client-cio:${ktorVersion}")`,
  ].join("\n");
  return (
    <div className={styles.install}>
      <div className={styles.code}>
        <pre tabIndex={0}>
          <code>{dependencies}</code>
        </pre>
        <CopyButton text={dependencies} />
      </div>
      <p className={styles.note}>
        Plus the Kotlin serialization plugin, see <Link to="/docs/getting-started">Getting Started</Link>. Latest
        release v{version}.{" "}
        <Link to={`https://github.com/flock-community/aigentic/releases/tag/v${version}`}>Release notes</Link>
      </p>
    </div>
  );
}
