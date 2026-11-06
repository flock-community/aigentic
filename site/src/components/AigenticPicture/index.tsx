import clsx from "clsx";
import Link from "@docusaurus/Link";
import { useCallback, useEffect, useRef, useState } from "react";
import useHydratedReducedMotion from "@site/src/hooks/useHydratedReducedMotion";
import {
  ARC_RANGES,
  CHANGES,
  CHART,
  COSTS,
  CX,
  CY,
  DURATION,
  GAP,
  GRID,
  POINTS,
  R,
  SCORES,
  START,
  STATIONS,
  STEPS,
  VIEW,
  arc,
  frameAt,
  point,
  restFrame,
  scoreY,
  type Center,
  type Frame,
} from "./timeline";
import styles from "./styles.module.css";

const REST_FRAME = restFrame();

const DESCRIPTION =
  "The Aigentic loop. Build and Run happen in your code with the Aigentic library. Record, Evaluate and Improve happen on the Aigentic Platform. Every run goes to the platform, and every improvement comes back to your code as the next version. In the example, $.total of an invoice extractor goes from 71% in version 1 to 92% in version 2 and 95% in version 3, on the same 120 invoices.";

const PlayIcon = () => (
  <svg viewBox="0 0 16 16" aria-hidden="true" focusable="false">
    <path d="M5 3.2v9.6l7.6-4.8z" fill="currentColor" />
  </svg>
);

const PauseIcon = () => (
  <svg viewBox="0 0 16 16" aria-hidden="true" focusable="false">
    <path d="M4.2 3.2h2.6v9.6H4.2zM9.2 3.2h2.6v9.6H9.2z" fill="currentColor" />
  </svg>
);

export default function AigenticPicture() {
  const reduceMotion = useHydratedReducedMotion();
  const [inView, setInView] = useState(false);
  const [pageVisible, setPageVisible] = useState(true);
  const [userPaused, setUserPaused] = useState<boolean | null>(null);
  const playing = inView && pageVisible && !(userPaused ?? reduceMotion);

  const figure = useRef<HTMLElement>(null);
  const visual = useRef<HTMLDivElement>(null);
  const arcs = useRef<(SVGPathElement | null)[]>([]);
  const head = useRef<SVGCircleElement>(null);
  const glow = useRef<SVGCircleElement>(null);
  const stations = useRef<(SVGGElement | null)[]>([]);
  const evaluateNode = useRef<SVGCircleElement>(null);
  const swap = useRef<HTMLDivElement>(null);
  const version = useRef<HTMLSpanElement>(null);
  const name = useRef<HTMLElement>(null);
  const detail = useRef<HTMLElement>(null);
  const note = useRef<HTMLSpanElement>(null);
  const area = useRef<SVGPathElement>(null);
  const line = useRef<SVGPathElement>(null);
  const points = useRef<(SVGGElement | null)[]>([]);
  const dots = useRef<(SVGCircleElement | null)[]>([]);
  const slots = useRef<(SVGGElement | null)[]>([]);
  const spark = useRef<HTMLSpanElement>(null);
  const steps = useRef<(HTMLLIElement | null)[]>([]);
  const time = useRef(START);
  const started = useRef(false);
  const shown = useRef<Center>(REST_FRAME.center);

  const apply = useCallback((frame: Frame) => {
    if (visual.current) visual.current.style.opacity = String(frame.opacity);
    frame.arcs.forEach((d, i) => arcs.current[i]?.setAttribute("d", d));
    const [hx, hy] = point(frame.angle);
    for (const circle of [head.current, glow.current]) {
      circle?.setAttribute("cx", hx.toFixed(2));
      circle?.setAttribute("cy", hy.toFixed(2));
    }
    stations.current.forEach((g, i) => {
      g?.classList.toggle(styles.reached, frame.reached[i]);
      g?.classList.toggle(styles.active, frame.active === i);
    });

    if (frame.center !== shown.current) {
      shown.current = frame.center;
      if (version.current) version.current.textContent = frame.center.version;
      if (name.current) name.current.textContent = frame.center.name;
      if (detail.current) detail.current.textContent = frame.center.detail;
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
    slots.current.forEach((g, i) => g?.classList.toggle(styles.slotIn, frame.landed[i]));

    const sparkEl = spark.current;
    const target = frame.spark ? dots.current[frame.spark.lap] : null;
    if (sparkEl && frame.spark && target && evaluateNode.current && visual.current) {
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

    steps.current.forEach((li, i) => {
      li?.classList.toggle(styles.stepOn, frame.step === i);
      li?.classList.toggle(styles.stepDone, frame.step > i);
      li?.style.setProperty("--p", frame.step === i ? frame.stepProgress.toFixed(3) : "0");
    });
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

  /* Until it first plays: the first frame when it will play by itself, the whole picture when motion is reduced. */
  useEffect(() => {
    if (started.current) return;
    time.current = START;
    apply(reduceMotion ? REST_FRAME : frameAt(START));
  }, [reduceMotion, apply]);

  useEffect(() => {
    if (!playing) return undefined;
    if (!started.current) {
      started.current = true;
      time.current = START;
    }
    let raf = 0;
    let last = performance.now();
    const tick = (now: number) => {
      time.current = (time.current + Math.min(0.1, (now - last) / 1000)) % DURATION;
      last = now;
      apply(frameAt(time.current));
      raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [playing, apply]);

  const rest = REST_FRAME;
  const [headX, headY] = point(rest.angle);

  return (
    <figure ref={figure} className={styles.figure}>
      <div ref={visual} className={styles.visual} role="img" aria-label={DESCRIPTION}>
        <div className={styles.ring}>
          <svg viewBox={`${VIEW.x} ${VIEW.y} ${VIEW.width} ${VIEW.height}`} aria-hidden="true" focusable="false">
            <rect
              className={styles.panelLib}
              x={VIEW.x + 0.5}
              y={VIEW.y + 0.5}
              width={CX - GAP - VIEW.x - 0.5}
              height={VIEW.height - 1}
              rx={18}
            />
            <rect
              className={styles.panelPlat}
              x={CX + GAP}
              y={VIEW.y + 0.5}
              width={VIEW.x + VIEW.width - CX - GAP - 0.5}
              height={VIEW.height - 1}
              rx={18}
            />
            <text className={clsx(styles.zone, styles.zoneLib)} x={VIEW.x + 22} y={22}>
              In your code
            </text>
            <text className={styles.zoneNote} x={VIEW.x + 22} y={42}>
              Aigentic library
            </text>
            <text className={clsx(styles.zone, styles.zonePlat)} x={VIEW.x + VIEW.width - 22} y={22} textAnchor="end">
              Aigentic Platform
            </text>
            <text className={styles.zoneNote} x={VIEW.x + VIEW.width - 22} y={42} textAnchor="end">
              Hosted or in your own environment
            </text>
            <path className={clsx(styles.track, styles.trackLib)} d={arc(180, 360)} />
            <path className={clsx(styles.track, styles.trackPlat)} d={arc(0, 180)} />
            {ARC_RANGES.map(([from, , zone], i) => (
              <path
                key={from}
                ref={(el) => {
                  arcs.current[i] = el;
                }}
                className={clsx(styles.progress, zone === "lib" ? styles.progressLib : styles.progressPlat)}
                d={rest.arcs[i]}
              />
            ))}
            <text className={styles.cross} x={CX} y={CY - R - 18} textAnchor="middle">
              every run →
            </text>
            <text className={styles.cross} x={CX} y={CY + R + 36} textAnchor="middle">
              ← next version
            </text>
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
                    rest.reached[i] && styles.reached,
                    rest.active === i && styles.active,
                  )}
                >
                  <circle className={styles.halo} cx={x} cy={y} r={19} />
                  <circle ref={i === 3 ? evaluateNode : undefined} className={styles.node} cx={x} cy={y} r={8} />
                  <text className={styles.label} x={lx} y={ly + 8} textAnchor={lib ? "end" : "start"}>
                    {STEPS[i].name}
                  </text>
                </g>
              );
            })}
            <circle ref={glow} className={styles.glow} cx={headX} cy={headY} r={16} />
            <circle ref={head} className={styles.head} cx={headX} cy={headY} r={6} />
          </svg>
          <div className={styles.center}>
            <div ref={swap} className={styles.swap}>
              <span ref={version} className={styles.pill}>
                {rest.center.version}
              </span>
              <strong ref={name} className={styles.stepName}>
                {rest.center.name}
              </strong>
              <code ref={detail} className={styles.detail}>
                {rest.center.detail}
              </code>
              <span ref={note} className={styles.note}>
                {rest.center.note}
              </span>
            </div>
          </div>
        </div>

        <div className={styles.scores}>
          <p className={styles.scoresTitle}>
            <code>$.total</code> per version, on the same 120 invoices
          </p>
          <svg className={styles.chart} viewBox="0 0 480 290" aria-hidden="true" focusable="false">
            {GRID.map((value) => (
              <g key={value}>
                <line className={styles.gridLine} x1={CHART.left} x2={CHART.right} y1={scoreY(value)} y2={scoreY(value)} />
                <text className={styles.axis} x={CHART.left - 8} y={scoreY(value) + 4} textAnchor="end">
                  {value}%
                </text>
              </g>
            ))}
            <path ref={area} className={styles.area} d={rest.area} />
            <path ref={line} className={styles.line} d={rest.line} />
            {SCORES.map((_, i) => (
              <g
                key={CHART.xs[i]}
                ref={(el) => {
                  slots.current[i] = el;
                }}
                className={clsx(styles.slot, rest.landed[i] && styles.slotIn)}
              >
                <rect className={styles.versionPill} x={CHART.xs[i] - 19} y={216} width={38} height={22} rx={11} />
                <text className={styles.versionText} x={CHART.xs[i]} y={231.5} textAnchor="middle">
                  v{i + 1}
                </text>
                <text className={styles.change} x={CHART.xs[i]} y={258} textAnchor="middle">
                  {CHANGES[i]}
                </text>
                <text className={styles.cost} x={CHART.xs[i]} y={278} textAnchor="middle">
                  {COSTS[i]}
                </text>
              </g>
            ))}
            {POINTS.map((p, i) => (
              <g
                key={p.x}
                ref={(el) => {
                  points.current[i] = el;
                }}
                className={clsx(styles.point, rest.landed[i] && styles.landed, rest.current === i && styles.current)}
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
                <text className={styles.value} x={p.x} y={p.y - 18} textAnchor="middle">
                  {SCORES[i]}%
                </text>
              </g>
            ))}
          </svg>
        </div>
        <span ref={spark} className={styles.spark} aria-hidden="true" />
      </div>

      <div className={styles.rail}>
        <button
          type="button"
          className={styles.play}
          onClick={() => setUserPaused(playing)}
          aria-label={playing ? "Pause the animation" : "Play the animation"}
        >
          {playing ? <PauseIcon /> : <PlayIcon />}
        </button>
        <ol className={styles.steps} aria-label="The steps, each linking to its docs">
          {STEPS.map((step, i) => (
            <li
              key={step.name}
              ref={(el) => {
                steps.current[i] = el;
              }}
              className={clsx(styles.step, step.zone === "plat" && styles.stepPlat)}
            >
              <Link className={styles.chip} to={step.to}>
                <span className={styles.number} aria-hidden="true">
                  {i < 5 ? i + 1 : "↻"}
                </span>
                {step.name}
                <i className={styles.fill} aria-hidden="true" />
              </Link>
            </li>
          ))}
        </ol>
      </div>
    </figure>
  );
}
