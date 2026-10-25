import { useEffect, useState } from "react";
import clsx from "clsx";
import styles from "./styles.module.css";

type CopyButtonProps = {
  text: string;
  label?: string;
  compact?: boolean;
  className?: string;
};

export default function CopyButton({ text, label = "Copy", compact = false, className }: CopyButtonProps) {
  const [status, setStatus] = useState<"idle" | "copied" | "failed">("idle");
  const copied = status === "copied";

  useEffect(() => {
    if (status === "idle") return;
    const timer = setTimeout(() => setStatus("idle"), 2000);
    return () => clearTimeout(timer);
  }, [status]);

  const copy = () =>
    (navigator.clipboard ? navigator.clipboard.writeText(text) : Promise.reject())
      .then(() => setStatus("copied"))
      .catch(() => setStatus("failed"));

  const statusLabel = status === "copied" ? "Copied" : status === "failed" ? "Couldn't copy" : label;

  return (
    <button
      type="button"
      className={clsx(styles.button, compact && styles.compact, copied && styles.copied, className)}
      onClick={copy}
      aria-label={compact ? `${statusLabel}: ${text}` : undefined}
    >
      <svg viewBox="0 0 16 16" aria-hidden="true">
        {copied ? (
          <path d="M3.5 8.5 6.5 11.5 12.5 4.5" fill="none" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
        ) : (
          <>
            <rect x="5.5" y="5.5" width="8" height="8" rx="1.5" fill="none" strokeWidth="1.3" />
            <path d="M10.5 3.5v-.5A1.5 1.5 0 0 0 9 1.5H4A1.5 1.5 0 0 0 2.5 3v5A1.5 1.5 0 0 0 4 9.5h.5" fill="none" strokeWidth="1.3" />
          </>
        )}
      </svg>
      {!compact && <span aria-live="polite">{statusLabel}</span>}
    </button>
  );
}
