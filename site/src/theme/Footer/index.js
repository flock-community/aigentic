import React from "react";
import { useThemeConfig } from "@docusaurus/theme-common";
import FooterLinks from "@theme/Footer/Links";
import FooterLogo from "@theme/Footer/Logo";
import FooterCopyright from "@theme/Footer/Copyright";
import FooterLayout from "@theme/Footer/Layout";
import Heading from "@theme/Heading";
import Link from "@docusaurus/Link";
import styles from "./footer.module.css";
import clsx from "clsx";
import { useLocation } from "@docusaurus/router";
import InstallSnippet from "@site/src/components/InstallSnippet";
import PixelMark from "@site/src/components/PixelMark";

function Footer() {
  const { footer } = useThemeConfig();
  if (!footer) {
    return null;
  }

  const location = useLocation();
  const isDocsPage = location.pathname.startsWith("/docs");

  const { copyright, links, logo, style } = footer;
  return (
    <>
      {!isDocsPage && (
        <section className="start-aigentic">
          <div className="container">
            <div className="row">
              <div className="col col--12">
                <div className="card card-border-bottom card-footer">
                  <div className="card-footer-header">
                    <Heading as="h2" className={clsx(styles.heading2, "footer-cta-title")}>
                      <PixelMark />
                      Start with Aigentic
                    </Heading>
                    <p>
                      Add the dependency, describe a task and run your first agent. Or start from
                      the Initializr and get a project with everything set up.
                    </p>
                  </div>
                  <div className="footer-install">
                    <InstallSnippet />
                  </div>
                  <div className="footer-action">
                    <Link
                      className={clsx(styles.button, styles.buttonPrimary)}
                      to="/docs/getting-started"
                    >
                      Get started
                    </Link>
                    <Link
                      className="footer-action-secondary"
                      to="https://github.com/flock-community/aigentic-initializr"
                    >
                      Initializr
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      )}
      <FooterLayout
        style={style}
        links={links && links.length > 0 && <FooterLinks links={links} />}
        logo={logo && <FooterLogo logo={logo} />}
        copyright={copyright && <FooterCopyright copyright={copyright} />}
      />
    </>
  );
}
export default React.memo(Footer);
