import { themes as prismThemes } from "prism-react-renderer";
import type { Config } from "@docusaurus/types";
import type * as Preset from "@docusaurus/preset-classic";

const config: Config = {
  title: "Aigentic",
  tagline: "Build and integrate AI agents with Kotlin",
  favicon: "img/favicon.png", // TODO: Replace with Aigentic favicon
  url: "https://aigentic.io/", // TODO: Update with actual URL
  baseUrl: "/",
  organizationName: "flock-community/aigentic", // TODO: Update with actual organization
  projectName: "aigentic",
  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",
  i18n: {
    defaultLocale: "en",
    locales: ["en"],
  },
  customFields: {
    version: "0.0.0-SNAPSHOT",
  },
  presets: [
    [
      "classic",
      {
        docs: {
          sidebarPath: "./sidebars.ts",
          editUrl:
            "https://github.com/flock-community/aigentic/tree/main/site/",
        },
        blog: {
          path: "./blog",
          routeBasePath: "/blog",
          showReadingTime: true,
        },
        theme: {
          customCss: [
            "./node_modules/@flock/black-sun/build/infima/custom.css",
            "./custom.css",
          ],
        },
        sitemap: {
          changefreq: "weekly",
          priority: 0.5,
          filename: "sitemap.xml",
        },
      } satisfies Preset.Options,
    ],
  ],
  themeConfig: {
    colorMode: {
      defaultMode: "dark",
      disableSwitch: true,
    },
    metadata: [
      {
        name: "description",
        content:
          "A Kotlin Multiplatform library that provides a powerful DSL for building and integrating AI agents into applications",
      },
      { property: "og:title", content: `Aigentic - AI Agents with Kotlin` },
      {
        property: "og:description",
        content:
          "A Kotlin Multiplatform library that provides a powerful DSL for building and integrating AI agents into applications",
      },
      { property: "og:image", content: "/img/og-image.png" },
      { property: "og:type", content: "website" },
      { property: "og:url", content: "https://aigentic.io/" },
    ],
    stylesheets: [
      {
        href: "https://fonts.googleapis.com/css2?family=Funnel+Display:wght@300..800&display=swap",
        type: "text/css",
      },
    ],
    // Replace with your project's social card
    image: "img/docusaurus-social-card.jpg",
    navbar: {
      title: "",
      logo: {
        alt: "Aigentic Logo",
        src: "img/aigentic-logo.svg", // TODO: Replace with Aigentic logo
      },
      items: [
        {
          to: "/vision",
          label: "Vision",
          position: "left",
        },
        {
          type: "docSidebar",
          sidebarId: "docsSidebar",
          position: "left",
          label: "Docs",
        },
        { to: "/blog", label: "Blog", position: "left" },
        {
          href: "https://github.com/flock-community/aigentic", // TODO: Update with actual GitHub URL
          position: "right",
          className: "header-github-link",
          html: '<svg width="22" height="22" viewBox="0 0 22 22" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><g clip-path="url(#clip0_1_50)"><path fill-rule="evenodd" clip-rule="evenodd" d="M10.9672 0C4.90263 0 0 5.04167 0 11.2789C0 16.2646 3.14129 20.485 7.49908 21.9787C8.04392 22.091 8.24349 21.736 8.24349 21.4374C8.24349 21.1759 8.22553 20.2796 8.22553 19.3458C5.17471 20.0182 4.53941 18.0013 4.53941 18.0013C4.04912 16.6941 3.32267 16.3581 3.32267 16.3581C2.32414 15.6672 3.39541 15.6672 3.39541 15.6672C4.50304 15.7419 5.08424 16.825 5.08424 16.825C6.06459 18.5428 7.64433 18.0574 8.27986 17.7586C8.37055 17.0303 8.66127 16.5261 8.96994 16.2461C6.53669 15.9846 3.97661 15.0136 3.97661 10.6812C3.97661 9.44877 4.41212 8.44044 5.1022 7.65623C4.99333 7.37619 4.61192 6.21821 5.21131 4.66835C5.21131 4.66835 6.13733 4.36952 8.22531 5.8261C9.11924 5.57921 10.0411 5.45362 10.9672 5.45256C11.8932 5.45256 12.8372 5.58342 13.7089 5.8261C15.7971 4.36952 16.7231 4.66835 16.7231 4.66835C17.3225 6.21821 16.9409 7.37619 16.832 7.65623C17.5403 8.44044 17.9578 9.44877 17.9578 10.6812C17.9578 15.0136 15.3978 15.9658 12.9463 16.2461C13.3459 16.6008 13.6907 17.273 13.6907 18.3375C13.6907 19.85 13.6728 21.0639 13.6728 21.4372C13.6728 21.736 13.8726 22.091 14.4172 21.9789C18.775 20.4847 21.9163 16.2646 21.9163 11.2789C21.9342 5.04167 17.0136 0 10.9672 0Z"/></g></svg>',
        },
      ],
    },
    footer: {
      style: "dark",
      logo: {
        href: "/",
        alt: "Aigentic Logo",
        src: "img/aigentic-logo.svg", // TODO: Replace with Aigentic logo
      },
      links: [
        {
          items: [
            {
              label: "Vision",
              to: "/vision",
            },
            {
              label: "Initializr",
              to: "https://github.com/flock-community/aigentic-initializr", // TODO: Update with actual GitHub URL
            },
          ],
        },
        {
          items: [
            {
              label: "Docs",
              to: "/docs/getting-started",
            },
            {
              label: "Blog",
              to: "/blog",
            },
          ],
        },
      ],
      copyright: `© ${new Date().getFullYear()} Aigentic. All rights reserved.`,
    },
    prism: {
      theme: prismThemes.vsLight,
      darkTheme: prismThemes.vsDark,
      // https://docusaurus.io/docs/markdown-features/code-blocks#supported-languages
      // Note: wirespec is not an official prism language, it's added through prism-include-languages.ts
      additionalLanguages: [
        "java",
        "typescript",
        "bash",
        "properties",
        "gradle",
      ],
    },
    algolia: {
      appId: "8GJ5M7UPF1",
      // apiKey: "ab006ab5e8aaea60cfcafc0dfa6e3a67",
      apiKey: "36710561274c40b93985864373d1aadb",
      // indexName: "wirespec_io_wijtzn3w54_pages",
      indexName: "aigentic",
      contextualSearch: true,
      searchPagePath: "search",
      insights: false,
    },
  } satisfies Preset.ThemeConfig,
  markdown: {
    format: "mdx",
    preprocessor: ({ fileContent }) => {
      const variables = {
        AIGENTIC_VERSION: "0.0.0-SNAPSHOT",
      };
      return fileContent.replaceAll(
        "{{AIGENTIC_VERSION}}",
        variables.AIGENTIC_VERSION,
      );
    },
  },
};

export default config;
