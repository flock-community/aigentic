import clsx from "clsx";
import Link from "@docusaurus/Link";
import styles from "./styles.module.css";

const DOMAIN: [number, number] = [80, 100];
const TICKS = [80, 90, 100];

/*
 * An illustrative comparison. Counts add up: after = before + fixed - regressed,
 * and percentages are correct / total, rounded.
 */
const ROWS = [
  { field: "$.invoiceNumber", total: 120, before: 109, after: 118, fixed: 10, regressed: 1 },
  { field: "$.total", total: 120, before: 114, after: 106, fixed: 1, regressed: 9 },
  { field: "$.dueDate", total: 120, before: 117, after: 118, fixed: 2, regressed: 1 },
  { field: "$.lines[*].amount", total: 412, before: 363, after: 383, fixed: 24, regressed: 4 },
];

const pct = (correct: number, total: number) => (correct / total) * 100;

const position = (value: number) =>
  `${Math.min(100, Math.max(0, ((value - DOMAIN[0]) / (DOMAIN[1] - DOMAIN[0])) * 100))}%`;

/* Fewer than six changed inputs can easily be chance (see Measuring well). */
const couldBeChance = (fixed: number, regressed: number) => fixed + regressed < 6;

const Trend = ({ worse }: { worse: boolean }) => (
  <svg viewBox="0 0 10 10" aria-hidden="true" focusable="false">
    <path d={worse ? "M5 2v6M2 5.5 5 8.5 8 5.5" : "M5 8V2M2 4.5 5 1.5 8 4.5"} fill="none" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

export default function VersionComparison() {
  return (
    <figure className={styles.card}>
      <figcaption className={styles.head}>
        <strong>Version 3 → version 4 of an invoice extractor</strong>
        <span>Example · ground truth for 120 invoices with 412 invoice lines</span>
        <span className={styles.legend} aria-hidden="true">
          <span>
            <i className={styles.before} /> version 3
          </span>
          <span>
            <i className={styles.after} /> version 4
          </span>
        </span>
      </figcaption>
      <table className={styles.table}>
        <thead className={styles.srOnly}>
          <tr>
            <th scope="col">Field</th>
            <th scope="col">Accuracy of version 3 and version 4, on a scale from 80% to 100%</th>
            <th scope="col">Result</th>
          </tr>
        </thead>
        <tbody>
          {ROWS.map(({ field, total, before, after, fixed, regressed }) => {
            const from = pct(before, total);
            const to = pct(after, total);
            const delta = Math.round(to) - Math.round(from);
            const chance = couldBeChance(fixed, regressed);
            const worse = !chance && delta < 0;
            return (
              <tr key={field}>
                <th scope="row">
                  <code>{field}</code>
                </th>
                <td className={styles.plot} aria-hidden="true">
                  <div className={styles.track}>
                    {TICKS.map((tick) => (
                      <span key={tick} className={styles.grid} style={{ left: position(tick) }} />
                    ))}
                    <span
                      className={clsx(styles.span, worse && styles.spanWorse)}
                      style={{
                        left: position(Math.min(from, to)),
                        width: `calc(${position(Math.max(from, to))} - ${position(Math.min(from, to))})`,
                      }}
                    />
                    <span className={styles.before} style={{ left: position(from) }} />
                    <span className={styles.after} style={{ left: position(to) }} />
                    <span className={styles.tooltip} style={{ left: position(to) }}>
                      {before} → {after} of {total} correct
                    </span>
                  </div>
                </td>
                <td className={styles.result}>
                  <span className={styles.values}>
                    <span className={styles.from}>{Math.round(from)}%</span>
                    <span aria-hidden="true"> → </span>
                    <span className={styles.srOnly}> to </span>
                    <span className={clsx(styles.to, worse && styles.toWorse)}>{Math.round(to)}%</span>
                    {!chance && (
                      <span className={clsx(styles.delta, worse ? styles.worse : styles.better)}>
                        <Trend worse={worse} />
                        {worse ? `${Math.abs(delta)} pts worse` : `${delta} pts better`}
                      </span>
                    )}
                  </span>
                  <span className={styles.detail}>
                    {fixed} fixed · {regressed} regressed
                    {chance && (
                      <Link className={styles.chance} to="/docs/platform/measuring-well#is-the-new-version-really-better">
                        could be chance
                      </Link>
                    )}
                  </span>
                  <span className={styles.srOnly}>
                    ({before} of {total} correct before, {after} after)
                  </span>
                </td>
              </tr>
            );
          })}
        </tbody>
        <tfoot aria-hidden="true">
          <tr>
            <td />
            <td className={styles.plot}>
              <div className={styles.axis}>
                {TICKS.map((tick) => (
                  <span key={tick} style={{ left: position(tick) }}>
                    {tick}%
                  </span>
                ))}
              </div>
            </td>
            <td />
          </tr>
        </tfoot>
      </table>
      <p className={styles.cost}>
        Cost per run: $0.0039 <span aria-hidden="true">→</span>
        <span className={styles.srOnly}> to </span> $0.0041 (+5%)
      </p>
    </figure>
  );
}
