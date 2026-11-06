import styles from "./styles.module.css";

const DOMAIN: [number, number] = [80, 100];

/* An illustrative comparison, consistent with the counts: percentages are correct / total, rounded. */
const ROWS = [
  { field: "$.invoiceNumber", total: 120, before: 109, after: 118, fixed: 10, regressed: 1 },
  { field: "$.total", total: 120, before: 114, after: 115, fixed: 2, regressed: 1 },
  { field: "$.lines[*].amount", total: 412, before: 363, after: 383, fixed: 24, regressed: 4 },
];

const pct = (correct: number, total: number) => (correct / total) * 100;

const position = (value: number) =>
  `${Math.min(100, Math.max(0, ((value - DOMAIN[0]) / (DOMAIN[1] - DOMAIN[0])) * 100))}%`;

/* Fewer than six changed inputs can easily be chance (see Measuring well). */
const couldBeChance = (fixed: number, regressed: number) => fixed + regressed < 6;

export default function VersionComparison() {
  return (
    <figure className={styles.card}>
      <figcaption className={styles.head}>
        <strong>Version 3 → version 4 of an invoice extractor</strong>
        <span>Example · golden set of 120 invoices with 412 lines</span>
      </figcaption>
      <table className={styles.table}>
        <thead className={styles.srOnly}>
          <tr>
            <th scope="col">Field</th>
            <th scope="col">Accuracy from version 3 to version 4, on a scale from 80% to 100%</th>
            <th scope="col">Result</th>
          </tr>
        </thead>
        <tbody>
          {ROWS.map(({ field, total, before, after, fixed, regressed }) => {
            const from = pct(before, total);
            const to = pct(after, total);
            return (
              <tr key={field}>
                <th scope="row">
                  <code>{field}</code>
                </th>
                <td className={styles.plot} aria-hidden="true">
                  <div className={styles.track}>
                    <span
                      className={styles.span}
                      style={{ left: position(Math.min(from, to)), width: `calc(${position(Math.max(from, to))} - ${position(Math.min(from, to))})` }}
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
                    <span className={styles.to}>{Math.round(to)}%</span>
                  </span>
                  <span className={styles.detail}>
                    {fixed} fixed · {regressed} regressed
                    {couldBeChance(fixed, regressed) && <span className={styles.chance}>could be chance</span>}
                  </span>
                  <span className={styles.srOnly}>
                    ({before} of {total} correct before, {after} after)
                  </span>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
      <p className={styles.cost}>
        Cost per run <span className={styles.from}>$0.0039</span>
        <span aria-hidden="true"> → </span>
        <span className={styles.srOnly}> to </span>
        <span className={styles.to}>$0.0041</span>
      </p>
      <div className={styles.legend} aria-hidden="true">
        <span>
          <i className={styles.before} /> version 3
        </span>
        <span>
          <i className={styles.after} /> version 4
        </span>
        <span className={styles.scale}>scale 80% to 100%</span>
      </div>
    </figure>
  );
}
