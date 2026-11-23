import styles from "./styles.module.css";

const MEASURED = 90;
const DOMAIN: [number, number] = [70, 100];
const TICKS = [70, 80, 90, 100];

/* 95% Wilson score intervals for 90% measured accuracy. */
const ROWS = [
  { inputs: 25, low: 72.5, high: 96.8 },
  { inputs: 50, low: 78.6, high: 95.7 },
  { inputs: 100, low: 82.6, high: 94.5 },
  { inputs: 200, low: 85.1, high: 93.4 },
  { inputs: 400, low: 86.7, high: 92.6 },
];

const position = (value: number) => `${((value - DOMAIN[0]) / (DOMAIN[1] - DOMAIN[0])) * 100}%`;

const percent = (value: number) => `${value.toFixed(1).replace(/\.0$/, "")}%`;

export default function MarginChart() {
  return (
    <figure className={styles.figure}>
      <figcaption className={styles.title}>
        <strong>Your agent scores 90%. How sure can you be?</strong>
        <span>The range the real accuracy likely falls in (95% interval), by number of inputs with ground truth</span>
      </figcaption>
      <table className={styles.table}>
        <thead>
          <tr>
            <th scope="col">Ground truth</th>
            <th scope="col" className={styles.plotHead}>
              <span className={styles.srOnly}>Range on a scale from 70% to 100%</span>
            </th>
            <th scope="col">Real accuracy likely</th>
          </tr>
        </thead>
        <tbody>
          {ROWS.map(({ inputs, low, high }) => (
            <tr key={inputs}>
              <th scope="row">{inputs} inputs</th>
              <td className={styles.plot} aria-hidden="true">
                <div className={styles.track}>
                  {TICKS.map((tick) => (
                    <span
                      key={tick}
                      className={tick === MEASURED ? styles.reference : styles.grid}
                      style={{ left: position(tick) }}
                    />
                  ))}
                  <span
                    className={styles.range}
                    style={{ left: position(low), width: `calc(${position(high)} - ${position(low)})` }}
                  />
                  <span className={styles.dot} style={{ left: position(MEASURED) }} />
                  <span className={styles.tooltip} style={{ left: position(MEASURED) }}>
                    90% measured on {inputs} inputs: really {percent(low)} to {percent(high)}
                  </span>
                </div>
              </td>
              <td className={styles.value}>
                {percent(low)} to {percent(high)}
              </td>
            </tr>
          ))}
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
    </figure>
  );
}
