import React from "react";
import { useThemeConfig } from "@docusaurus/theme-common";
import FooterLinks from "@theme/Footer/Links";
import FooterLogo from "@theme/Footer/Logo";
import FooterCopyright from "@theme/Footer/Copyright";
import FooterLayout from "@theme/Footer/Layout";
import { useLocation } from "@docusaurus/router";
import StartSection from "@site/src/components/StartSection";

function Footer() {
  const { footer } = useThemeConfig();
  if (!footer) {
    return null;
  }

  const location = useLocation();
  const showStart = !location.pathname.startsWith("/docs") && location.pathname !== "/";

  const { copyright, links, logo, style } = footer;
  return (
    <>
      {showStart && <StartSection />}
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
