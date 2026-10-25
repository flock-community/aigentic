import Link from "@docusaurus/Link";
import Heading from "@theme/Heading";
import InstallSnippet from "../InstallSnippet";
import ModuleMap from "../ModuleMap";
import PixelMark from "../PixelMark";
import styles from "./styles.module.css";

export default function StartSection({ showModules = false }: { showModules?: boolean }) {
  return (
    <section className="start-aigentic" aria-label="Start with Aigentic">
      <div className="container">
        <div className="row">
          <div className="col col--12">
            <div className="card card-border-bottom card-footer">
              <div className="card-footer-header">
                <Heading as="h2" className="footer-cta-title">
                  <PixelMark />
                  Start with Aigentic
                </Heading>
                {showModules ? (
                  <p>
                    Add core, a provider and a Ktor engine, then pick the modules your agent needs. Everything is on
                    Maven Central under <code>community.flock.aigentic</code>.
                  </p>
                ) : (
                  <p>
                    Add the dependency, describe a task and run your first agent. Or start from the Initializr and get a
                    project with everything set up.
                  </p>
                )}
              </div>
              <div className="footer-install">
                <InstallSnippet />
              </div>
              {showModules && (
                <div className={styles.modules}>
                  <ModuleMap />
                </div>
              )}
              <div className="footer-action">
                <Link className="footer-action-primary" to="/docs/getting-started">
                  Get started
                </Link>
                <Link className="footer-action-secondary" to="https://github.com/flock-community/aigentic-initializr">
                  Initializr
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
