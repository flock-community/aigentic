import React from "react";
import Layout from "@theme/Layout";
import styles from "./vision.module.css";
import Heading from "@theme/Heading";
import Link from "@docusaurus/Link";
import clsx from "clsx";

export default function VisionPage() {
  return (
    <Layout
      title="Aigentic AI Agents"
      description="Build powerful AI agents with an intuitive Kotlin DSL, streamline LLM integration, and create intelligent applications with ease"
      image="/img/code-snippet.jpg"
    >
      <main className={styles.visionMain}>
        <section className={styles.visionSection}>
          <div className={styles.visionContent}>
            <h1 className={styles.title}>
              Aigentic <br /> <span>vision</span>{" "}
            </h1>
            <p className={styles.paragraph}>
              Aigentic envisions a future where AI integration is seamless and accessible
              through an intuitive Kotlin DSL. Our library serves as a bridge between
              developers and large language models, providing a unified interface that
              abstracts away the complexities of working with different LLM providers.
              This model-agnostic approach empowers developers to focus on creating
              intelligent applications without being locked into specific AI platforms.
            </p>
            <p className={styles.paragraph}>
              Central to the Aigentic vision is the democratization of AI agent development.
              By providing a type-safe, expressive DSL, we enable developers to create,
              deploy, and manage sophisticated AI agents with minimal boilerplate code.
              Our library streamlines the process of defining agent behaviors, integrating
              tools, and handling conversations, making AI development more efficient and
              enjoyable. Aigentic's multiplatform support ensures that these capabilities
              are available across different environments.
            </p>
            <p className={styles.paragraph}>
              Furthermore, Aigentic is committed to fostering an ecosystem where AI agents
              can be easily composed, extended, and shared. We believe in building
              intelligent systems that are not only powerful but also transparent and
              controllable. By providing robust tools for testing, debugging, and monitoring
              AI agents, we ensure that developers can create reliable and trustworthy
              AI-powered applications. Aigentic aims to be at the forefront of the AI
              revolution, enabling the next generation of intelligent software that
              enhances human capabilities rather than replacing them.
            </p>
          </div>
        </section>
      </main>
    </Layout>
  );
}
