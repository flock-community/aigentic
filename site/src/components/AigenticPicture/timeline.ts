/*
 * Geometry and timeline of the Aigentic loop. Everything the animation shows at a moment is a pure
 * function of the time, so the server renders the complete picture and the browser can jump anywhere.
 */

export type Zone = "lib" | "plat";
export type Step = { name: string; zone: Zone; to: string };
export type Center = { version: string; name: string; detail: string; note: string };

export const STEPS: Step[] = [
  { name: "Build", zone: "lib", to: "/docs/dsl/agent" },
  { name: "Run", zone: "lib", to: "/docs/getting-started" },
  { name: "Record", zone: "plat", to: "/docs/platform/runs" },
  { name: "Evaluate", zone: "plat", to: "/docs/platform/evaluations" },
  { name: "Improve", zone: "plat", to: "/docs/platform/fix-assistant" },
  { name: "Next version", zone: "lib", to: "/docs/platform/evaluations#compare-config-versions" },
];

/* Ring: angles run clockwise from the top. Your code is the left half, the platform the right half. */
export const CX = 320;
export const CY = 250;
export const R = 180;
export const VIEW = { x: -50, y: -10, width: 740, height: 520 };
export const GAP = 8;
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

/* Chart: $.total per version, on a 60% to 100% scale. */
export const SCORES = [71, 92, 95];
export const CHANGES = ["first version", "+ addInstruction(…)", "mediaResolution(HIGH)"];
export const COSTS = ["$0.0039 per run", "$0.0040 per run", "$0.0052 per run"];
export const GRID = [60, 80, 100];
export const CHART = { base: 200, top: 40, low: 60, high: 100, left: 64, right: 470, xs: [100, 250, 400] };
export const scoreY = (value: number) =>
  CHART.base - ((value - CHART.low) / (CHART.high - CHART.low)) * (CHART.base - CHART.top);
export const POINTS = SCORES.map((score, i) => ({ x: CHART.xs[i], y: scoreY(score) }));

const clamp = (v: number) => Math.min(1, Math.max(0, v));
const seg = (t: number, a: number, b: number) => clamp((t - a) / (b - a));
const easeInOut = (p: number) => (p < 0.5 ? 4 * p * p * p : 1 - Math.pow(-2 * p + 2, 3) / 2);
const easeOut = (p: number) => 1 - Math.pow(1 - p, 3);

/* Three laps: the first is slow, the next two are quicker because the steps are familiar by then. */
const LAPS = [6.4, 4.2, 4.2].map((d) => d * 1.6);
const LAP_START = [0, LAPS[0], LAPS[0] + LAPS[1]];
const ARRIVE = [0, 0.17, 0.36, 0.53, 0.72, 0.94];
const KEYFRAMES: [number, number][] = [
  [0, 225], [0.06, 225], [0.17, 315], [0.23, 315], [0.36, 405], [0.42, 405],
  [0.53, 450], [0.59, 450], [0.72, 495], [0.78, 495], [0.94, 585], [1, 585],
];
const END = LAP_START[2] + 0.59 * LAPS[2];
const FLY = 0.8;
const DRAW = 0.7;
export const DURATION = END + 2.6;
export const START = 0.4;

const evaluatedAt = (lap: number) => LAP_START[lap] + ARRIVE[3] * LAPS[lap];
const landsAt = (lap: number) => evaluatedAt(lap) + FLY;
export const REST = landsAt(2) + DRAW + 0.15;

const lapAt = (t: number) => {
  const time = Math.min(t, END);
  const lap = time >= LAP_START[2] ? 2 : time >= LAP_START[1] ? 1 : 0;
  return { lap, f: (time - LAP_START[lap]) / LAPS[lap] };
};

const angleAt = (f: number) => {
  for (let i = 0; i < KEYFRAMES.length - 1; i++) {
    const [f0, a0] = KEYFRAMES[i];
    const [f1, a1] = KEYFRAMES[i + 1];
    if (f <= f1) return a0 + (a1 - a0) * easeInOut(seg(f, f0, f1));
  }
  return 585;
};

const stepOf = (f: number) => {
  let i = ARRIVE.length - 1;
  while (i > 0 && f < ARRIVE[i]) i--;
  return i;
};

const nextV2: Center = { version: "v2", name: "Build", detail: "+ addInstruction(…)", note: "Apply the fix in your code" };
const nextV3: Center = { version: "v3", name: "Build", detail: "mediaResolution(HIGH)", note: "Apply the fix in your code" };
const CONTENT: Center[][] = [
  [
    { version: "v1", name: "Build", detail: "agent<Unit, Invoice> { }", note: "A typed agent, any model" },
    { version: "v1", name: "Run", detail: "extractor.start(invoice)", note: "Inside your own app" },
    { version: "v1", name: "Record", detail: "run #118 · $0.0039", note: "Every run, with its cost" },
    { version: "v1", name: "Evaluate", detail: "$.total 71%", note: "Accuracy per field" },
    { version: "v1", name: "Improve", detail: "+ addInstruction(…)", note: "The fix assistant's best change" },
    nextV2,
  ],
  [
    nextV2,
    { version: "v2", name: "Run", detail: "extractor.start(invoice)", note: "Same inputs, new version" },
    { version: "v2", name: "Record", detail: "run #241 · $0.0040", note: "Every run, with its cost" },
    { version: "v2", name: "Evaluate", detail: "$.total 92%", note: "Up from 71%" },
    { version: "v2", name: "Improve", detail: "mediaResolution(HIGH)", note: "Small print gets misread" },
    nextV3,
  ],
  [
    nextV3,
    { version: "v3", name: "Run", detail: "extractor.start(invoice)", note: "Same inputs, new version" },
    { version: "v3", name: "Record", detail: "run #362 · $0.0052", note: "Sharper images cost a bit more" },
    { version: "v3", name: "Evaluate", detail: "$.total 95%", note: "Up from 92%" },
  ],
];

export type Frame = {
  opacity: number;
  angle: number;
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
  const { lap, f } = lapAt(t);
  const angle = angleAt(f);
  const step = stepOf(f);
  const flying = SCORES.findIndex((_, i) => t >= evaluatedAt(i) && t < landsAt(i));
  return {
    opacity: t < 0.35 ? t / 0.35 : t > DURATION - 0.4 ? clamp((DURATION - t) / 0.4) : 1,
    angle,
    arcs: ARC_RANGES.map(([from, to]) => arc(from, Math.min(to, angle))),
    reached: STATIONS.map((_, i) => f >= ARRIVE[i]),
    active: step === 5 ? 0 : step,
    center: CONTENT[lap][step],
    ...chartAt(t),
    spark: flying < 0 ? null : { lap: flying, progress: easeInOut((t - evaluatedAt(flying)) / FLY) },
    step,
    stepProgress: seg(f, ARRIVE[step], step < 5 ? ARRIVE[step + 1] : 1),
  };
}

/* The still picture for the server, for reduced motion and before playing: the whole loop and all three versions. */
export function restFrame(): Frame {
  return {
    ...frameAt(REST),
    arcs: ARC_RANGES.map(([from, to]) => arc(from, to)),
    reached: STATIONS.map(() => true),
    step: -1,
    stepProgress: 0,
  };
}
