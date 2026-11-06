import clsx from "clsx";
import Link from "@docusaurus/Link";
import useIsBrowser from "@docusaurus/useIsBrowser";
import { useCallback, useEffect, useRef, useState } from "react";
import useHydratedReducedMotion from "@site/src/hooks/useHydratedReducedMotion";
import {
  ARC_RANGES,
  CHANGES,
  CHART,
  COSTS,
  CROSS_BOTTOM,
  CROSS_TOP,
  CY,
  DURATION,
  GRID,
  PEEKS,
  POINTS,
  R,
  SCORES,
  STATIONS,
  STEPS,
  VIEW,
  arc,
  finishedFrame,
  frameAt,
  point,
  scoreY,
  type Center,
  type Frame,
} from "./timeline";
import styles from "./styles.module.css";

const FINISHED = finishedFrame();
const CENTER_TOP = ((CY - VIEW.y) / VIEW.height) * 100;

const DESCRIPTION =
  "The Aigentic loop. Build and Run happen in your code with the Aigentic library. Record, Evaluate and Improve happen on the Aigentic Platform. Every run goes to the platform, and every improvement comes back to your code as the next version. In the example, the accuracy of $.total of an invoice extractor goes from 71% in version 1 to 92% in version 2 and 95% in version 3, on the same 120 invoices.";

/* waiting: ready to play once it scrolls into view; done: resting on the finished picture. */
type Phase = "waiting" | "playing" | "paused" | "done";

const Icon = ({ d }: { d: string }) => (
  <svg viewBox="0 0 16 16" aria-hidden="true" focusable="false">
    <path d={d} fill="currentColor" />
  </svg>
);
const PLAY = "M5 3.2v9.6l7.6-4.8z";
const PAUSE = "M4.2 3.2h2.6v9.6H4.2zM9.2 3.2h2.6v9.6H9.2z";
const REPLAY =
  "M8 2.5a5.5 5.5 0 1 1-5.2 3.7l1.4.5A4 4 0 1 0 8 4v2L4.8 3.25 8 .5z";

const Arrow = ({ back = false }: { back?: boolean }) => (
  <svg className={styles.arrow} viewBox="0 0 14 10" aria-hidden="true" focusable="false">
    <path
      d={back ? "M13 5H1.5M5 1 1 5l4 4" : "M1 5h11.5M9 1l4 4-4 4"}
      fill="none"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

export default function AigenticPicture() {
  const isBrowser = useIsBrowser();
  const reduceMotion = useHydratedReducedMotion();
  const [phase, setPhase] = useState<Phase>("done");
  const [inView, setInView] = useState(false);
  const [pageVisible, setPageVisible] = useState(true);
  const [step, setStep] = useState(-1);
  const running = phase === "playing" && inView && pageVisible;

  const figure = useRef<HTMLElement>(null);
  const visual = useRef<HTMLDivElement>(null);
  const ringSvg = useRef<SVGSVGElement>(null);
  const chartSvg = useRef<SVGSVGElement>(null);
  const arcs = useRef<(SVGPathElement | null)[]>([]);
  const head = useRef<SVGGElement>(null);
  const stations = useRef<(SVGGElement | null)[]>([]);
  const evaluateNode = useRef<SVGCircleElement>(null);
  const swap = useRef<HTMLDivElement>(null);
  const version = useRef<HTMLSpanElement>(null);
  const name = useRef<HTMLSpanElement>(null);
  const detail = useRef<HTMLElement>(null);
  const note = useRef<HTMLSpanElement>(null);
  const area = useRef<SVGPathElement>(null);
  const line = useRef<SVGPathElement>(null);
  const points = useRef<(SVGGElement | null)[]>([]);
  const dots = useRef<(SVGCircleElement | null)[]>([]);
  const versions = useRef<(HTMLLIElement | null)[]>([]);
  const fills = useRef<(HTMLElement | null)[]>([]);
  const spark = useRef<HTMLSpanElement>(null);
  const time = useRef(DURATION);
  const started = useRef(false);
  const shownCenter = useRef<Center>(FINISHED.center);
  const shownStep = useRef(FINISHED.step);

  const apply = useCallback((frame: Frame, withSpark: boolean) => {
    frame.arcs.forEach((d, i) => arcs.current[i]?.setAttribute("d", d));
    const [hx, hy] = point(frame.angle);
    if (head.current) {
      head.current.setAttribute("transform", `translate(${hx.toFixed(2)} ${hy.toFixed(2)})`);
      head.current.style.opacity = String(frame.head);
    }
    stations.current.forEach((g, i) => {
      g?.classList.toggle(styles.reached, frame.reached[i]);
      g?.classList.toggle(styles.active, frame.active === i);
    });

    if (frame.center !== shownCenter.current) {
      shownCenter.current = frame.center;
      if (version.current) version.current.textContent = frame.center.version;
      if (name.current) name.current.textContent = frame.center.name;
      if (detail.current) {
        detail.current.textContent = frame.center.detail;
        detail.current.classList.toggle(styles.score, Boolean(frame.center.score));
      }
      if (note.current) note.current.textContent = frame.center.note;
      const el = swap.current;
      if (el) {
        el.classList.remove(styles.swapIn);
        void el.offsetWidth;
        el.classList.add(styles.swapIn);
      }
    }

    line.current?.setAttribute("d", frame.line);
    area.current?.setAttribute("d", frame.area);
    points.current.forEach((g, i) => {
      g?.classList.toggle(styles.landed, frame.landed[i]);
      g?.classList.toggle(styles.current, frame.current === i);
    });
    versions.current.forEach((li, i) => li?.classList.toggle(styles.versionIn, frame.landed[i]));

    const sparkEl = spark.current;
    const target = frame.spark ? dots.current[frame.spark.lap] : null;
    if (sparkEl && withSpark && frame.spark && target && evaluateNode.current && visual.current) {
      const box = visual.current.getBoundingClientRect();
      const centerOf = (el: Element) => {
        const r = el.getBoundingClientRect();
        return { x: r.left - box.left + r.width / 2, y: r.top - box.top + r.height / 2 };
      };
      const a = centerOf(evaluateNode.current);
      const b = centerOf(target);
      const p = frame.spark.progress;
      const u = 1 - p;
      const mx = (a.x + b.x) / 2;
      const my = Math.min(a.y, b.y) - 50;
      const x = u * u * a.x + 2 * u * p * mx + p * p * b.x;
      const y = u * u * a.y + 2 * u * p * my + p * p * b.y;
      sparkEl.style.transform = `translate(${x.toFixed(1)}px, ${y.toFixed(1)}px)`;
      sparkEl.style.opacity = "1";
    } else if (sparkEl) {
      sparkEl.style.opacity = "0";
    }

    fills.current.forEach((el, i) =>
      el?.style.setProperty("--p", frame.step === i ? frame.stepProgress.toFixed(3) : "0"),
    );
    if (frame.step !== shownStep.current) {
      shownStep.current = frame.step;
      setStep(frame.step);
    }
  }, []);

  /* SVG text keeps one rendered size however wide the drawing is: --k undoes the drawing's scale. */
  useEffect(() => {
    const scaled: [SVGSVGElement | null, number][] = [
      [ringSvg.current, VIEW.width],
      [chartSvg.current, CHART.width],
    ];
    const measure = () =>
      scaled.forEach(([svg, width]) => {
        const rendered = svg?.getBoundingClientRect().width ?? 0;
        if (svg && rendered > 0) svg.style.setProperty("--k", (width / rendered).toFixed(3));
      });
    const observer = new ResizeObserver(measure);
    scaled.forEach(([svg]) => svg && observer.observe(svg));
    measure();
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    const el = figure.current;
    if (!el) return undefined;
    const observer = new IntersectionObserver(([entry]) => setInView(entry.isIntersecting), { threshold: 0.3 });
    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    const onChange = () => setPageVisible(!document.hidden);
    document.addEventListener("visibilitychange", onChange);
    return () => document.removeEventListener("visibilitychange", onChange);
  }, []);

  /* Before it first plays: the first frame when it will play by itself, the finished picture when motion is reduced. */
  useEffect(() => {
    if (started.current) return;
    if (reduceMotion) {
      time.current = DURATION;
      apply(FINISHED, false);
      setPhase("done");
    } else {
      time.current = 0;
      apply(frameAt(0), false);
      setPhase("waiting");
    }
  }, [reduceMotion, apply]);

  useEffect(() => {
    if (phase === "waiting" && inView && !reduceMotion) setPhase("playing");
  }, [phase, inView, reduceMotion]);

  useEffect(() => {
    if (!running) return undefined;
    started.current = true;
    let raf = 0;
    let last = performance.now();
    const tick = (now: number) => {
      time.current = Math.min(DURATION, time.current + Math.min(0.1, (now - last) / 1000));
      last = now;
      if (time.current >= DURATION) {
        apply(FINISHED, false);
        setPhase("done");
        return;
      }
      apply(frameAt(time.current), true);
      raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => {
      cancelAnimationFrame(raf);
      if (spark.current) spark.current.style.opacity = "0";
    };
  }, [running, apply]);

  const togglePlay = () => {
    if (phase === "playing") {
      setPhase("paused");
      return;
    }
    if (phase === "done") {
      time.current = 0;
      apply(frameAt(0), false);
    }
    started.current = true;
    setPhase("playing");
  };

  const jumpTo = (i: number) => {
    started.current = true;
    time.current = PEEKS[i];
    apply(frameAt(PEEKS[i]), false);
    setPhase("paused");
  };

  const playLabel =
    phase === "playing" ? "Pause the animation" : phase === "done" ? "Replay the animation" : "Play the animation";
  const current = step >= 0 ? STEPS[step] : null;
  const f = FINISHED;
  const [headX, headY] = point(f.angle);

  return (
    <figure ref={figure} className={styles.figure}>
      <div ref={visual} className={styles.visual} role="img" aria-label={DESCRIPTION}>
        <div className={styles.ring}>
          <div className={styles.panels}>
            <span className={styles.panelLib} />
            <span className={styles.panelPlat} />
          </div>
          <div className={styles.zones}>
            <p className={styles.zoneLib}>
              <strong>In your code</strong>
              <span>Aigentic library</span>
            </p>
            <p className={styles.zonePlat}>
              <strong>Aigentic Platform</strong>
              <span>Hosted by Flock or in your own environment</span>
            </p>
          </div>
          <div className={styles.art}>
            <svg
              ref={ringSvg}
              className={styles.ringSvg}
              viewBox={`${VIEW.x} ${VIEW.y} ${VIEW.width} ${VIEW.height}`}
              aria-hidden="true"
              focusable="false"
            >
              <path className={clsx(styles.track, styles.trackLib)} d={arc(180, 360)} />
              <path className={clsx(styles.track, styles.trackPlat)} d={arc(0, 180)} />
              {ARC_RANGES.map(([from, , zone], i) => (
                <path
                  key={from}
                  ref={(el) => {
                    arcs.current[i] = el;
                  }}
                  className={clsx(styles.progress, zone === "lib" ? styles.progressLib : styles.progressPlat)}
                  d={f.arcs[i]}
                />
              ))}
              {STATIONS.map((deg, i) => {
                const [x, y] = point(deg);
                const [lx, ly] = point(deg, R + 30);
                const lib = deg % 360 >= 180;
                return (
                  <g
                    key={deg}
                    ref={(el) => {
                      stations.current[i] = el;
                    }}
                    className={clsx(
                      styles.station,
                      lib ? styles.stationLib : styles.stationPlat,
                      f.reached[i] && styles.reached,
                      f.active === i && styles.active,
                    )}
                  >
                    <circle className={styles.halo} cx={x} cy={y} r={19} />
                    <circle ref={i === 3 ? evaluateNode : undefined} className={styles.node} cx={x} cy={y} r={8} />
                    <text className={styles.label} x={lx} y={ly} dy="0.35em" textAnchor={lib ? "end" : "start"}>
                      {STEPS[i].name}
                    </text>
                  </g>
                );
              })}
              <g ref={head} transform={`translate(${headX} ${headY})`} style={{ opacity: f.head }}>
                <circle className={styles.glow} r={16} />
                <circle className={styles.head} r={6} />
              </g>
            </svg>
            <span className={styles.crossing} style={{ top: `${CROSS_TOP}%` }}>
              every run <Arrow />
            </span>
            <span className={styles.crossing} style={{ top: `${CROSS_BOTTOM}%` }}>
              <Arrow back /> next version
            </span>
            <div className={styles.center} style={{ top: `${CENTER_TOP}%` }}>
              <div ref={swap} className={styles.swap}>
                <span className={styles.meta}>
                  <span ref={version} className={styles.pill}>
                    {f.center.version}
                  </span>
                  <span ref={name} className={styles.stepName}>
                    {f.center.name}
                  </span>
                </span>
                <code ref={detail} className={clsx(styles.detail, f.center.score && styles.score)}>
                  {f.center.detail}
                </code>
                <span ref={note} className={styles.note}>
                  {f.center.note}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className={styles.scores}>
          <p className={styles.scoresTitle}>
            Accuracy of <code>$.total</code> per version, on the same 120 invoices
          </p>
          <svg
            ref={chartSvg}
            className={styles.chart}
            viewBox={`0 0 ${CHART.width} ${CHART.height}`}
            aria-hidden="true"
            focusable="false"
          >
            {GRID.map((value) => (
              <g key={value}>
                <line className={styles.gridLine} x1={0} x2={CHART.width} y1={scoreY(value)} y2={scoreY(value)} />
                <text className={styles.axis} x={0} y={scoreY(value)} dy="-0.55em">
                  {value}%
                </text>
              </g>
            ))}
            <path ref={area} className={styles.area} d={f.area} />
            <path ref={line} className={styles.line} d={f.line} />
            {POINTS.map((p, i) => (
              <g
                key={p.x}
                ref={(el) => {
                  points.current[i] = el;
                }}
                className={clsx(styles.point, f.landed[i] && styles.landed, f.current === i && styles.current)}
              >
                <circle className={styles.halo} cx={p.x} cy={p.y} r={17} />
                <circle
                  ref={(el) => {
                    dots.current[i] = el;
                  }}
                  className={styles.dot}
                  cx={p.x}
                  cy={p.y}
                  r={7}
                />
                <text className={styles.value} x={p.x} y={p.y} dy="-1.1em" textAnchor="middle">
                  {SCORES[i]}%
                </text>
              </g>
            ))}
          </svg>
          <ol className={styles.versions}>
            {SCORES.map((score, i) => (
              <li
                key={score}
                ref={(el) => {
                  versions.current[i] = el;
                }}
                className={clsx(styles.version, f.landed[i] && styles.versionIn)}
              >
                <span className={styles.versionPill}>v{i + 1}</span>
                <span className={styles.change}>
                  {CHANGES[i].code ? (
                    <code className={styles.long}>{CHANGES[i].long}</code>
                  ) : (
                    <span className={styles.long}>{CHANGES[i].long}</span>
                  )}
                  <span className={styles.short}>{CHANGES[i].short}</span>
                </span>
                <span className={styles.cost}>
                  <span className={styles.long}>{COSTS[i]} per run</span>
                  <span className={styles.short}>{COSTS[i]}/run</span>
                </span>
              </li>
            ))}
          </ol>
        </div>
        <span ref={spark} className={styles.spark} />
      </div>

      <div className={styles.rail}>
        {isBrowser && (
          <button type="button" className={styles.play} onClick={togglePlay} aria-label={playLabel}>
            <Icon d={phase === "playing" ? PAUSE : phase === "done" ? REPLAY : PLAY} />
          </button>
        )}
        <ol className={styles.steps}>
          {STEPS.map((s, i) => {
            const chipClass = clsx(
              styles.chip,
              s.zone === "plat" && styles.chipPlat,
              step === i && styles.chipOn,
              step > i && styles.chipDone,
            );
            const content = (
              <>
                <span className={styles.number} aria-hidden="true">
                  {i + 1}
                </span>
                {s.name}
                <i
                  ref={(el) => {
                    fills.current[i] = el;
                  }}
                  className={styles.fill}
                  aria-hidden="true"
                />
              </>
            );
            return (
              <li key={s.name}>
                {isBrowser ? (
                  <button
                    type="button"
                    className={chipClass}
                    onClick={() => jumpTo(i)}
                    aria-current={step === i ? "step" : undefined}
                    aria-label={`Show step ${i + 1}, ${s.name}`}
                  >
                    {content}
                  </button>
                ) : (
                  <span className={clsx(chipClass, styles.chipStatic)}>{content}</span>
                )}
              </li>
            );
          })}
        </ol>
      </div>
      <p className={styles.docs}>
        {current ? (
          <>
            <strong className={current.zone === "plat" ? styles.docsPlat : undefined}>{current.name}.</strong>{" "}
            {current.text}{" "}
            <Link className={styles.docsLink} to={current.to}>
              {current.page}
              <Arrow />
            </Link>
          </>
        ) : (
          <>
            Each step has its own page in the docs. To follow one agent through all of them, read{" "}
            <Link className={styles.docsLink} to="/docs/lifecycle">
              From prototype to production
              <Arrow />
            </Link>
          </>
        )}
      </p>
    </figure>
  );
}
