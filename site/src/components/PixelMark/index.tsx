import clsx from "clsx";
import styles from "./styles.module.css";

const DOTS = [0, 1, 2].flatMap((row) => [0, 1, 2].map((column) => ({ row, column })));

export default function PixelMark({ className }: { className?: string }) {
  return (
    <svg className={clsx(styles.mark, className)} viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      {DOTS.map(({ row, column }) => (
        <circle key={`${row}-${column}`} cx={4 + column * 8} cy={4 + row * 8} r={1.3 + (row + column) * 0.6} />
      ))}
    </svg>
  );
}
