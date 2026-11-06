/*
 * Geometry and timeline of the Aigentic loop. Everything the animation shows at a moment is a pure
 * function of the time, so the server can render the finished picture and a step button can jump anywhere.
 * It plays three laps once (versions v1 to v3), closes the loop and rests on the finished picture.
 */

export type Zone = "lib" | "plat";
export type Step = { name: string; zone: Zone; text: string; to: string; page: string };
export type Center = { version: string; name: string; detail: string; note: string; score?: boolean };

export const STEPS: Step[] = [
  {
    name: "Build",
    zone: "lib",
    text: "Describe a typed agent in Kotlin, with its task, tools and model.",
    to: "/docs/dsl/agent",
    page: "Agent DSL",
  },
  {
    name: "Run",
    zone: "lib",
    text: "It runs inside your own app, with your own model keys.",
    to: "/docs/getting-started",
    page: "Getting Started",
  },
  {
    name: "Record",
    zone: "plat",
    text: "Every run goes to the platform, with its cost and config version.",
    to: "/docs/platform/runs",
    page: "Runs, versions and costs",
  },
  {
    name: "Evaluate",
    zone: "plat",
    text: "The platform scores every output field against your ground truth.",
    to: "/docs/platform/evaluations",
    page: "Evaluations",
  },
  {
    name: "Improve",
    zone: "plat",
    text: "The fix assistant proposes the change that scores best, and you apply it in your code.",
    to: "/docs/platform/fix-assistant",
    page: "Fix assistant",
  },
];

/* Ring: angles run clockwise from the top. Your code is the left half, the platform the right half. */
export const CX = 320;
export const CY = 250;
export const R = 180;
export const VIEW = { x: -50, y: -10, width: 740, height: 520 };
export const STATIONS = [225, 315, 405, 450, 495];
export const ARC_RANGES: [number, number, Zone][] = [
  [225, 360, "lib"],
  [360, 540, "plat"],
  [540, 585, "lib"],
];

export const point = (deg: number, radius = R): [number, number] => {
  const a = (deg * Math.PI) / 180;
  return [CX + radius * Math.sin(a), CY - radius * Math.cos(a)];
};

export const arc = (from: number, to: number): string => {
  if (to - from < 0.05) return "";
  const [x0, y0] = point(from);
  const [x1, y1] = point(to);
  return `M ${x0.toFixed(2)} ${y0.toFixed(2)} A ${R} ${R} 0 ${to - from > 180 ? 1 : 0} 1 ${x1.toFixed(2)} ${y1.toFixed(2)}`;
};

/* Where the hand-off labels sit, as a share of the ring's box. */
export const CROSS_TOP = ((CY - R - 26 - VIEW.y) / VIEW.height) * 100;
export const CROSS_BOTTOM = ((CY + R + 26 - VIEW.y) / VIEW.height) * 100;

/* Chart: accuracy of $.total per version, on a 60% to 100% scale. The versions sit at the centers of three equal columns. */
export const SCORES = [71, 92, 95];
export const CHANGES = [
  { long: "first version", short: "first version", code: false },
  { long: "+ addInstruction(…)", short: "+ one instruction", code: true },
  { long: "mediaResolution(HIGH)", short: "+ sharper images", code: true },
];
export const COSTS = ["$0.0039 per run", "$0.0040 per run", "$0.0052 per run"];
export const GRID = [60, 80, 100];
export const CHART = { width: 480, height: 212, base: 200, top: 40, low: 60, high: 100, xs: [80, 240, 400] };
export const scoreY = (value: number) =>
  CHART.base - ((value - CHART.low) / (CHART.high - CHART.low)) * (CHART.base - CHART.top);
export const POINTS = SCORES.map((score, i) => ({ x: CHART.xs[i], y: scoreY(score) }));

const clamp = (v: number) => Math.min(1, Math.max(0, v));
const seg = (t: number, a: number, b: number) => clamp((t - a) / (b - a));
const easeInOut = (p: number) => (p < 0.5 ? 4 * p * p * p : 1 - Math.pow(-2 * p + 2, 3) / 2);
const easeOut = (p: number) => 1 - Math.pow(1 - p, 3);

/*
 * Per lap and station: how long the pulse rests there, then how long it travels to the next station.
 * The first lap takes its time. The repeat laps hurry through Run and Record, which only repeat,
 * and keep Evaluate and Improve on screen for at least two seconds. The last lap ends at Evaluate.
 */
const LAPS: [number, number][][] = [
  [[0.9, 0.8], [0.9, 0.8], [0.9, 0.8], [2.0, 0.6], [1.6, 1.1]],
  [[1.1, 0.6], [0.4, 0.5], [0.4, 0.5], [2.0, 0.6], [1.6, 1.0]],
  [[1.1, 0.6], [0.4, 0.5], [0.4, 0.5], [2.0, 0]],
];
const FLY = 0.8;
const DRAW = 0.7;
const SETTLE = 1.0;

type Stop = { lap: number; station: number; angle: number; arrive: number; leave: number };
const STOPS: Stop[] = [];
{
  let t = 0;
  LAPS.forEach((stations, lap) =>
    stations.forEach(([dwell, travel], station) => {
      STOPS.push({ lap, station, angle: STATIONS[station] + 360 * lap, arrive: t, leave: t + dwell });
      t += dwell + travel;
    }),
  );
}
const LAST = STOPS[STOPS.length - 1];
const SETTLE_START = LAST.leave;
export const DURATION = SETTLE_START + SETTLE;

const evaluations = STOPS.filter((s) => s.station === 3);
const landsAt = (lap: number) => evaluations[lap].arrive + FLY;

const stopIndexAt = (t: number) => {
  let i = 0;
  while (i < STOPS.length - 1 && t >= STOPS[i + 1].arrive) i++;
  return i;
};

const angleAt = (t: number, i: number) => {
  const stop = STOPS[i];
  const next = STOPS[i + 1];
  if (!next || t <= stop.leave) return stop.angle;
  return stop.angle + (next.angle - stop.angle) * easeInOut(seg(t, stop.leave, next.arrive));
};

const changed = (version: string, detail: string): Center => ({
  version,
  name: "Build",
  detail,
  note: "You apply the change in your code",
});
/* \u200b marks where long code may wrap in the small center of the ring. */
const CONTENT: Center[][] = [
  [
    { version: "v1", name: "Build", detail: "agent<Unit, Invoice>", note: "A typed agent, any model" },
    { version: "v1", name: "Run", detail: "invoiceExtractor.\u200bstart(…)", note: "Inside your own app" },
    { version: "v1", name: "Record", detail: "run #118 · $0.0039", note: "Every run, with its cost" },
    { version: "v1", name: "Evaluate", detail: "$.total 71%", note: "Accuracy against your ground truth", score: true },
    { version: "v1", name: "Improve", detail: "+ addInstruction(…)", note: "The fix assistant's best change" },
  ],
  [
    changed("v2", "+ addInstruction(…)"),
    { version: "v2", name: "Run", detail: "invoiceExtractor.\u200bstart(…)", note: "Same inputs, new version" },
    { version: "v2", name: "Record", detail: "run #241 · $0.0040", note: "Every run, with its cost" },
    { version: "v2", name: "Evaluate", detail: "$.total 92%", note: "Up from 71%", score: true },
    { version: "v2", name: "Improve", detail: "mediaResolution\u200b(HIGH)", note: "Small print gets misread" },
  ],
  [
    changed("v3", "mediaResolution\u200b(HIGH)"),
    { version: "v3", name: "Run", detail: "invoiceExtractor.\u200bstart(…)", note: "Same inputs, new version" },
    { version: "v3", name: "Record", detail: "run #362 · $0.0052", note: "Sharper images cost a bit more" },
    { version: "v3", name: "Evaluate", detail: "$.total 95%", note: "Up from 92%", score: true },
  ],
];
const FINISHED: Center = {
  version: "v3",
  name: "Evaluate",
  detail: "$.total 95%",
  note: "Up from 71% in v1, on the same inputs",
  score: true,
};

export type Frame = {
  angle: number;
  head: number;
  arcs: string[];
  reached: boolean[];
  active: number;
  center: Center;
  landed: boolean[];
  current: number;
  line: string;
  area: string;
  spark: { lap: number; progress: number } | null;
  step: number;
  stepProgress: number;
};

const chartAt = (t: number) => {
  const landed = SCORES.map((_, i) => t >= landsAt(i));
  const current = landed.lastIndexOf(true);
  const drawn = POINTS.slice(0, current + 1).map((p, i) => {
    if (i === 0) return p;
    const q = easeOut(seg(t, landsAt(i), landsAt(i) + DRAW));
    const from = POINTS[i - 1];
    return { x: from.x + (p.x - from.x) * q, y: from.y + (p.y - from.y) * q };
  });
  if (drawn.length < 2) return { landed, current, line: "", area: "" };
  const line = drawn.map((p, i) => `${i ? "L" : "M"} ${p.x.toFixed(1)} ${p.y.toFixed(1)}`).join(" ");
  const last = drawn[drawn.length - 1];
  return { landed, current, line, area: `${line} L ${last.x.toFixed(1)} ${CHART.base} L ${drawn[0].x} ${CHART.base} Z` };
};

export function frameAt(t: number): Frame {
  const i = stopIndexAt(t);
  const stop = STOPS[i];
  const settle = seg(t, SETTLE_START, DURATION);
  const absolute = angleAt(t, i);
  const local = absolute - 360 * stop.lap;
  /* While settling, the loop closes behind the resting pulse. */
  const drawnTo = settle > 0 ? local + (585 - local) * easeOut(settle) : local;
  const next = STOPS[i + 1];
  const flying = SCORES.findIndex((_, lap) => t >= evaluations[lap].arrive && t < landsAt(lap));
  return {
    angle: local,
    head: 1 - settle,
    arcs: ARC_RANGES.map(([from, to]) => arc(from, Math.min(to, drawnTo))),
    reached: STATIONS.map((deg, s) => (s <= stop.station ? true : drawnTo >= deg)),
    active: settle > 0 ? -1 : stop.station,
    center: CONTENT[stop.lap][stop.station],
    ...chartAt(t),
    spark: flying < 0 ? null : { lap: flying, progress: easeInOut((t - evaluations[flying].arrive) / FLY) },
    step: stop.station,
    stepProgress: seg(t, stop.arrive, next ? next.arrive : DURATION),
  };
}

/* The finished picture: the whole loop closed and all three versions scored. Shown by the server, at the end and with reduced motion. */
export function finishedFrame(): Frame {
  return {
    ...frameAt(DURATION),
    head: 0,
    arcs: ARC_RANGES.map(([from, to]) => arc(from, to)),
    reached: STATIONS.map(() => true),
    active: -1,
    center: FINISHED,
    step: -1,
    stepProgress: 0,
  };
}

/* A frame that shows each step well when a visitor jumps to it: in the first lap, after the step has done its thing. */
export const PEEKS = STOPS.filter((s) => s.lap === 0).map((s) =>
  s.station === 3 ? landsAt(0) + DRAW + 0.1 : s.arrive + Math.min(0.5, s.leave - s.arrive),
);
