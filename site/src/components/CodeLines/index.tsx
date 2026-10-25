import { useEffect, useRef } from "react";
import clsx from "clsx";
import { motion, useReducedMotion } from "motion/react";
import { Highlight } from "prism-react-renderer";
import { usePrismTheme } from "@docusaurus/theme-common";
import styles from "./styles.module.css";

type Swap = {
  key: string | number;
  lines: number[];
  animate: boolean;
};

type CodeLinesProps = {
  code: string;
  activeLines?: number[];
  swap?: Swap;
  minLines?: number;
  followActive?: boolean;
  className?: string;
};

export default function CodeLines({
  code,
  activeLines = [],
  swap,
  minLines,
  followActive = false,
  className,
}: CodeLinesProps) {
  const prismTheme = usePrismTheme();
  const preRef = useRef<HTMLPreElement>(null);
  const reduceMotion = useReducedMotion();
  const firstActive = activeLines[0];

  useEffect(() => {
    const pre = preRef.current;
    if (!followActive || !pre || firstActive === undefined || pre.scrollHeight <= pre.clientHeight) return;
    const line = pre.querySelector<HTMLElement>(`[data-line="${firstActive}"]`);
    if (!line) return;
    pre.scrollTo({ top: line.offsetTop - pre.clientHeight / 3, behavior: reduceMotion ? "auto" : "smooth" });
  }, [followActive, firstActive, reduceMotion]);

  return (
    <Highlight theme={prismTheme} code={code} language="kotlin">
      {({ tokens, getLineProps, getTokenProps }) => (
        <pre
          ref={preRef}
          tabIndex={0}
          className={clsx(styles.code, className)}
          style={minLines ? { minHeight: `calc(${minLines} * 1.65em + 32px)` } : undefined}
        >
          <code>
            {tokens.map((line, lineIndex) => {
              const content = line.map((token, key) => <span key={key} {...getTokenProps({ token })} />);
              const { className: lineClassName, ...lineProps } = getLineProps({ line });
              return (
                <div
                  key={lineIndex}
                  data-line={lineIndex}
                  {...lineProps}
                  className={clsx(lineClassName, styles.line, activeLines.includes(lineIndex) && styles.lineActive)}
                >
                  <span className={styles.lineNumber} aria-hidden="true">
                    {lineIndex + 1}
                  </span>
                  {swap?.lines.includes(lineIndex) ? (
                    <motion.span
                      key={`${swap.key}-${lineIndex}`}
                      className={styles.lineContent}
                      initial={swap.animate ? { opacity: 0, y: 6 } : false}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.35, delay: (lineIndex - swap.lines[0]) * 0.05 }}
                    >
                      {content}
                    </motion.span>
                  ) : (
                    <span className={styles.lineContent}>{content}</span>
                  )}
                </div>
              );
            })}
          </code>
        </pre>
      )}
    </Highlight>
  );
}
