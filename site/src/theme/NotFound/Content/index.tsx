import clsx from "clsx";
import Link from "@docusaurus/Link";
import Heading from "@theme/Heading";
import MascotPortrait from "@site/src/components/Mascot";
import styles from "./styles.module.css";

export default function NotFoundContent({ className }: { className?: string }) {
  return (
    <main className={clsx("container", styles.notFound, className)}>
      <MascotPortrait playlist={["look_around", "idle", "wave", "idle", "tilt_curious", "idle"]} className={styles.bird} />
      <Heading as="h1" className={styles.title}>
        Page not found
      </Heading>
      <p className={styles.text}>This page flew off. It may have moved, or the link is out of date.</p>
      <div className={styles.actions}>
        <Link className={clsx(styles.action, styles.primary)} to="/">
          Back to home
        </Link>
        <Link className={styles.action} to="/docs/getting-started">
          Read the docs
        </Link>
      </div>
    </main>
  );
}
