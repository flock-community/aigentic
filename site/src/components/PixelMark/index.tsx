import clsx from "clsx";
import styles from "./styles.module.css";

export default function PixelMark({ className }: { className?: string }) {
  return (
    <svg className={clsx(styles.mark, className)} viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <g transform="rotate(-28 12 12)">
        <rect x="11.5" y="2" width="6.5" height="6.5" rx="1.8" />
        <rect x="4.5" y="7.5" width="8" height="8" rx="2" />
        <rect x="11" y="14" width="8" height="8" rx="2" />
      </g>
    </svg>
  );
}
