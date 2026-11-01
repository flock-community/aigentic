import clsx from "clsx";
import { motion } from "motion/react";
import useHydratedReducedMotion from "@site/src/hooks/useHydratedReducedMotion";
import styles from "./styles.module.css";

const BIRDS = [
  { cx: 6, cy: 3.4, r: 1.6, x: -40, y: -22, delay: 0.08 },
  { cx: 6, cy: 20.6, r: 1.6, x: -38, y: 24, delay: 0.12 },
  { cx: 13, cy: 7.6, r: 2.1, x: -34, y: -14, delay: 0.2 },
  { cx: 13, cy: 16.4, r: 2.1, x: -30, y: 18, delay: 0.24 },
  { cx: 20, cy: 12, r: 2.7, x: -26, y: 10, delay: 0.32 },
];

export default function FlockMark({ className }: { className?: string }) {
  const reduceMotion = useHydratedReducedMotion();
  return (
    <svg className={clsx(styles.mark, className)} viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      {BIRDS.map(({ cx, cy, r, x, y, delay }) => (
        <motion.circle
          key={`${cx}-${cy}`}
          cx={cx}
          cy={cy}
          r={r}
          initial={{ x, y, opacity: 0 }}
          whileInView={{ x: 0, y: 0, opacity: 1 }}
          viewport={{ once: true, amount: 0.6 }}
          transition={reduceMotion ? { duration: 0 } : { duration: 0.9, delay, ease: [0.2, 0.7, 0.2, 1] }}
        />
      ))}
    </svg>
  );
}
