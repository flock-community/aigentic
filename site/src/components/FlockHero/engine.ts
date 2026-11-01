import { createPoseClock, loadMascot, updatePose, type Mascot } from "../Mascot/mascot";
import { SHADING, createProjection, dotRadius, rasterize, type Projection } from "../Mascot/halftone";
import type { HeroLayout } from "./layout";

const TIMING = { build: 2500, hold: 16500, dissolve: 2500, swarm: 4500, stagger: 0.62, firstBuild: 2000 };
const PERIOD = TIMING.build + TIMING.hold + TIMING.dissolve + TIMING.swarm;
const DISSOLVE_START = TIMING.build + TIMING.hold;

const FLOCK = { spread: 1.4, speed: 1.1, follow: 1.4, settlePull: 8, particleSize: 1.8 };

const ROTATION = { baseYaw: -2.356, range: (42 * Math.PI) / 180, rate: 0.35 * 0.17 };

const ADAPT = { warmup: 1500, samples: 90, slowInterval: 22, liteRatio: 1 };

const LOOK = { intensity: 0.8, gridOpacity: 0.9, textCalm: 0.7, accent: "240,188,66" };

type Point = { x: number; y: number };

type Grid = {
  gap: number;
  cols: number;
  rows: number;
  depth: Float32Array;
  shade: Float32Array;
  calm: Float32Array;
  owner: Int32Array;
  visible: Float32Array;
};

type SwarmFrame = {
  t: number;
  x: number;
  y: number;
  cos: number;
  sin: number;
  sx: number;
  sy: number;
  drift: number;
  spread: number;
};

type Buckets = { count: number[]; data: Float32Array[]; stride: number };

type Flock = {
  count: number;
  x: Float32Array;
  y: Float32Array;
  offset: Float32Array;
  rate: Float32Array;
  delayIn: Float32Array;
  delayOut: Float32Array;
  role: Int8Array;
  cell: Int32Array;
  jitterX: Float32Array;
  jitterY: Float32Array;
  settled: Uint8Array;
  specks: Buckets;
  flyers: Buckets;
};

export type FlockHero = {
  resize: (layout: HeroLayout, pixelRatio: number) => void;
  setPointer: (pointer: Point | null) => void;
  start: () => void;
  stop: () => void;
  destroy: () => void;
};

const clamp01 = (v: number) => Math.min(1, Math.max(0, v));
const smooth = (v: number) => v * v * (3 - 2 * v);
const gaussian = () => Math.random() + Math.random() + Math.random() + Math.random() - 2;
const target: Point = { x: 0, y: 0 };
const swarm: SwarmFrame = { t: 0, x: 0, y: 0, cos: 1, sin: 0, sx: 0, sy: 0, drift: 0, spread: 0 };

const createBuckets = (levels: number, items: number, stride: number): Buckets => ({
  count: new Array(levels).fill(0),
  data: Array.from({ length: levels }, () => new Float32Array(items * stride)),
  stride,
});

function calmAt(layout: HeroLayout, x: number, y: number) {
  const dx = (x - layout.calmX) / layout.calmRx;
  const dy = (y - layout.calmY) / layout.calmRy;
  const text = clamp01((Math.sqrt(dx * dx + dy * dy) - 0.5) / 0.7);
  return Math.min(text, 0.3 + 0.7 * clamp01((y - 30) / 90));
}

function buildGrid(layout: HeroLayout, pixelRatio: number, background: HTMLCanvasElement): Grid {
  const gap = layout.gap;
  const cols = Math.ceil(layout.width / gap);
  const rows = Math.ceil(layout.height / gap);
  const size = cols * rows;
  const calm = new Float32Array(size);
  background.width = Math.max(1, Math.round(layout.width * pixelRatio));
  background.height = Math.max(1, Math.round(layout.height * pixelRatio));
  const levels: number[][] = [[], [], [], []];
  for (let j = 0; j < rows; j++) {
    for (let i = 0; i < cols; i++) {
      const x = gap / 2 + i * gap;
      const y = gap / 2 + j * gap;
      const c = calmAt(layout, x, y);
      calm[j * cols + i] = c;
      levels[Math.min(3, Math.floor(c * 4))].push(x, y);
    }
  }
  const g = background.getContext("2d");
  if (g) {
    g.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);
    levels.forEach((points, level) => {
      g.fillStyle = `rgba(255,255,255,${((0.03 + level * 0.011) * LOOK.intensity * LOOK.gridOpacity).toFixed(3)})`;
      for (let k = 0; k < points.length; k += 2) g.fillRect(points[k] - 0.7, points[k + 1] - 0.7, 1.4, 1.4);
    });
  }
  return {
    gap,
    cols,
    rows,
    depth: new Float32Array(size),
    shade: new Float32Array(size),
    calm,
    owner: new Int32Array(size).fill(-1),
    visible: new Float32Array(size),
  };
}

function updateSwarmFrame(time: number, layout: HeroLayout) {
  const t = time * FLOCK.speed;
  const a1 = t * 0.00016 + 0.6;
  const a2 = t * 0.00033 + 1;
  const b1 = t * 0.00019 + 0.5;
  const b2 = t * 0.00041;
  const cx = 760 + 520 * Math.sin(a1) + 90 * Math.sin(a2);
  const cy = 420 + 190 * Math.sin(b1) + 50 * Math.sin(b2);
  const vx = 520 * 0.00016 * Math.cos(a1) + 90 * 0.00033 * Math.cos(a2);
  const vy = 190 * 0.00019 * Math.cos(b1) + 50 * 0.00041 * Math.cos(b2);
  const angle = Math.atan2(vy, vx) * 0.6 + 0.2 * Math.sin(t * 0.00037 + 2);
  const sx = 210 + 80 * Math.sin(t * 0.00021 + 0.4);
  const sy = 66 + 34 * Math.sin(t * 0.00029 + 1.1);
  swarm.t = t;
  swarm.x = (cx / 1440) * layout.width;
  swarm.y = layout.swarmTop + (cy / 810) * layout.swarmHeight;
  swarm.cos = Math.cos(angle);
  swarm.sin = Math.sin(angle);
  swarm.sx = sx;
  swarm.sy = sy;
  swarm.drift = 30 * Math.cos(t * 0.0004);
  swarm.spread = FLOCK.spread * layout.flockScale;
}

function swarmTarget(flock: Flock, i: number): Point {
  const ox = flock.offset[i * 3];
  const oy = flock.offset[i * 3 + 1];
  const oz = flock.offset[i * 3 + 2];
  const t = swarm.t;
  const lx = ox * swarm.sx + 38 * Math.sin(oy * 3.1 - t * 0.0007) + oz * swarm.drift;
  const ly = oy * swarm.sy + 52 * Math.sin(ox * 2.6 + t * 0.0009) + 22 * oz * Math.sin(t * 0.0005 + ox);
  target.x = swarm.x + swarm.spread * (lx * swarm.cos - ly * swarm.sin);
  target.y = swarm.y + swarm.spread * (lx * swarm.sin + ly * swarm.cos);
  return target;
}

function createFlock(count: number, layout: HeroLayout, time: number): Flock {
  const flock: Flock = {
    count,
    x: new Float32Array(count),
    y: new Float32Array(count),
    offset: new Float32Array(count * 3),
    rate: new Float32Array(count),
    delayIn: new Float32Array(count),
    delayOut: new Float32Array(count),
    role: new Int8Array(count).fill(-1),
    cell: new Int32Array(count).fill(-1),
    jitterX: new Float32Array(count),
    jitterY: new Float32Array(count),
    settled: new Uint8Array(count),
    specks: createBuckets(10, count, 2),
    flyers: createBuckets(6, count, 3),
  };
  updateSwarmFrame(time, layout);
  for (let i = 0; i < count; i++) {
    flock.offset[i * 3] = gaussian() * 0.85;
    flock.offset[i * 3 + 1] = gaussian() * 0.95;
    flock.offset[i * 3 + 2] = gaussian();
    flock.rate[i] = 0.0032 + Math.random() * 0.004;
    const p = swarmTarget(flock, i);
    flock.x[i] = p.x;
    flock.y[i] = p.y;
  }
  return flock;
}

function shuffled(length: number) {
  const order = Array.from({ length }, (_, i) => i);
  for (let i = length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [order[i], order[j]] = [order[j], order[i]];
  }
  return order;
}

function coveredCells(grid: Grid) {
  const cells: number[] = [];
  grid.depth.forEach((z, idx) => {
    if (z !== Infinity) cells.push(idx);
  });
  return cells;
}

function sweepDelays(flock: Flock, grid: Grid, out: Float32Array) {
  let lo = Infinity;
  let hi = -Infinity;
  for (let i = 0; i < flock.count; i++) {
    if (flock.cell[i] < 0) continue;
    const column = flock.cell[i] % grid.cols;
    lo = Math.min(lo, column);
    hi = Math.max(hi, column);
  }
  const span = Math.max(1, hi - lo);
  for (let i = 0; i < flock.count; i++) {
    if (flock.cell[i] >= 0) out[i] = (((flock.cell[i] % grid.cols) - lo) / span) * 0.75 + Math.random() * 0.25;
  }
}

function assignRoles(flock: Flock, grid: Grid) {
  const cells = coveredCells(grid);
  grid.owner.fill(-1);
  shuffled(flock.count).forEach((i, rank) => {
    flock.settled[i] = 0;
    flock.delayOut[i] = Math.random();
    if (!cells.length) {
      flock.role[i] = -1;
      return;
    }
    if (rank < cells.length) {
      flock.role[i] = 1;
      flock.cell[i] = cells[rank];
      grid.owner[cells[rank]] = i;
      flock.jitterX[i] = 0;
      flock.jitterY[i] = 0;
    } else {
      flock.role[i] = 0;
      flock.cell[i] = cells[Math.floor(Math.random() * cells.length)];
      flock.jitterX[i] = (Math.random() - 0.5) * grid.gap * 0.8;
      flock.jitterY[i] = (Math.random() - 0.5) * grid.gap * 0.8;
    }
  });
  sweepDelays(flock, grid, flock.delayIn);
}

function rebindVisibleCells(flock: Flock, grid: Grid) {
  const cells = coveredCells(grid);
  if (!cells.length) return;
  const { gap, cols, owner } = grid;
  const spare: number[] = [];
  for (let i = 0; i < flock.count; i++) {
    if (flock.role[i] === 1 && grid.depth[flock.cell[i]] === Infinity) {
      if (owner[flock.cell[i]] === i) owner[flock.cell[i]] = -1;
      const cell = cells[Math.floor(Math.random() * cells.length)];
      flock.role[i] = 0;
      flock.cell[i] = cell;
      flock.jitterX[i] = (Math.random() - 0.5) * gap * 0.8;
      flock.jitterY[i] = (Math.random() - 0.5) * gap * 0.8;
      flock.x[i] = gap / 2 + (cell % cols) * gap + flock.jitterX[i];
      flock.y[i] = gap / 2 + Math.floor(cell / cols) * gap + flock.jitterY[i];
      flock.settled[i] = 0;
    }
    if (flock.role[i] === 0) spare.push(i);
  }
  cells.forEach((cell) => {
    if (owner[cell] >= 0) return;
    const i = spare.pop();
    if (i === undefined) return;
    flock.role[i] = 1;
    flock.cell[i] = cell;
    owner[cell] = i;
    flock.jitterX[i] = 0;
    flock.jitterY[i] = 0;
    flock.x[i] = gap / 2 + (cell % cols) * gap;
    flock.y[i] = gap / 2 + Math.floor(cell / cols) * gap;
    flock.settled[i] = 1;
  });
  sweepDelays(flock, grid, flock.delayOut);
}

function keepAssignments(flock: Flock, previous: Grid, next: Grid) {
  const size = next.owner.length;
  next.owner.set(previous.owner.subarray(0, Math.min(previous.owner.length, size)));
  const owned: number[] = [];
  next.owner.forEach((owner, cell) => {
    if (owner >= 0) owned.push(cell);
  });
  for (let i = 0; i < flock.count; i++) {
    if (flock.cell[i] < size) continue;
    flock.settled[i] = 0;
    if (!owned.length) {
      flock.role[i] = -1;
      flock.cell[i] = -1;
      continue;
    }
    flock.role[i] = 0;
    flock.cell[i] = owned[Math.floor(Math.random() * owned.length)];
    flock.jitterX[i] = 0;
    flock.jitterY[i] = 0;
  }
}

function pushPoint(buckets: Buckets, level: number, x: number, y: number) {
  const offset = buckets.count[level] * buckets.stride;
  buckets.data[level][offset] = x;
  buckets.data[level][offset + 1] = y;
  buckets.count[level] += 1;
}

function pushDisc(buckets: Buckets, level: number, x: number, y: number, radius: number) {
  pushPoint(buckets, level, x, y);
  buckets.data[level][(buckets.count[level] - 1) * buckets.stride + 2] = radius;
}

export function createFlockHero(
  canvas: HTMLCanvasElement,
  background: HTMLCanvasElement,
  mascotUrl: string,
  reducedMotion: boolean,
): FlockHero {
  const ctx = canvas.getContext("2d");
  const poseClock = createPoseClock();
  const light = [-0.42, -0.56, 0.71];
  let mascot: Mascot | null = null;
  let projection: Projection | null = null;
  let layout: HeroLayout | null = null;
  let grid: Grid | null = null;
  let flock: Flock | null = null;
  let pointer: Point | null = null;
  let pixelRatio = 1;
  let clock = 0;
  let last = 0;
  let frameId = 0;
  let requestedRatio = 1;
  let lite = false;
  let skipFrame = false;
  let pendingStep = 0;
  let samples = 0;
  let averageInterval = 16;
  let running = false;
  let destroyed = false;
  let cycleStart: number | null = null;
  let cycleIndex = -1;
  let reboundIndex = -1;

  const updateLight = (seconds: number) => {
    if (!layout) return;
    const midY = layout.footY - SHADING.footToCenter * layout.scale;
    const [x, d, u] = pointer
      ? [(pointer.x - layout.birdX) / 420, -0.75, (midY - pointer.y) / 420]
      : [Math.sin(seconds * 0.3) * 0.7, -0.65, 0.5 + 0.25 * Math.cos(seconds * 0.3)];
    const length = Math.hypot(x, d, u) || 1;
    const ease = reducedMotion ? 1 : 0.08;
    light[0] += (x / length - light[0]) * ease;
    light[1] += (d / length - light[1]) * ease;
    light[2] += (u / length - light[2]) * ease;
  };

  const cellRadius = (g: Grid, idx: number) => {
    if (g.depth[idx] === Infinity) return dotRadius(g.gap, 1) * 0.5;
    return dotRadius(g.gap, clamp01(g.shade[idx] * LOOK.intensity) * (1 - LOOK.textCalm + LOOK.textCalm * g.calm[idx]));
  };

  const render = (time: number, step: number) => {
    if (!ctx || !layout || !grid || !flock) return;
    const g = grid;
    const f = flock;
    const l = layout;
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);
    g.depth.fill(Infinity);
    const seconds = time / 1000;
    if (mascot && projection) {
      updatePose(mascot, poseClock, seconds);
      updateLight(seconds);
      rasterize(mascot, g, l, ROTATION.baseYaw + Math.sin(seconds * ROTATION.rate) * ROTATION.range, light, projection);
    }

    let phase = 0;
    let active = false;
    if (mascot && !reducedMotion) {
      if (cycleStart === null) cycleStart = time + TIMING.firstBuild;
      const elapsed = time - cycleStart;
      if (elapsed >= 0) {
        const index = Math.floor(elapsed / PERIOD);
        phase = elapsed % PERIOD;
        if (index !== cycleIndex) {
          cycleIndex = index;
          assignRoles(f, g);
        }
        active = true;
      }
    }
    const dissolving = active && phase >= DISSOLVE_START;
    if (dissolving && reboundIndex !== cycleIndex) {
      reboundIndex = cycleIndex;
      rebindVisibleCells(f, g);
    }
    const looseFade = reducedMotion
      ? 1
      : !active
        ? 0
        : dissolving
          ? 1 - clamp01((phase - DISSOLVE_START) / 600)
          : clamp01((phase - TIMING.build - 300) / 500);

    const speckRadius = FLOCK.particleSize / 2;
    f.specks.count.fill(0);
    f.flyers.count.fill(0);
    if (!reducedMotion) {
      updateSwarmFrame(time, l);
      for (let i = 0; i < f.count; i++) {
        const role = f.role[i];
        const cell = f.cell[i];
        let weight = 0;
        let gx = 0;
        let gy = 0;
        if (active && role >= 0 && cell >= 0) {
          weight = dissolving
            ? 1 -
              Math.pow(
                clamp01(
                  (phase - DISSOLVE_START - f.delayOut[i] * TIMING.dissolve * TIMING.stagger) / (TIMING.dissolve * (1 - TIMING.stagger)),
                ),
                2,
              )
            : smooth(clamp01((phase - f.delayIn[i] * TIMING.build * TIMING.stagger) / (TIMING.build * (1 - TIMING.stagger))));
          gx = g.gap / 2 + (cell % g.cols) * g.gap + f.jitterX[i];
          gy = g.gap / 2 + Math.floor(cell / g.cols) * g.gap + f.jitterY[i];
        }
        let tx = gx;
        let ty = gy;
        if (weight < 1) {
          const free = swarmTarget(f, i);
          tx = free.x + (gx - free.x) * weight;
          ty = free.y + (gy - free.y) * weight;
        }
        if (step > 0) {
          const pull = Math.min(1, step * f.rate[i] * FLOCK.follow * (1 + FLOCK.settlePull * weight));
          f.x[i] += (tx - f.x[i]) * pull;
          f.y[i] += (ty - f.y[i]) * pull;
        }
        const x = f.x[i];
        const y = f.y[i];
        if (x < -6 || x > l.width + 6 || y < -6 || y > l.height + 6) {
          f.settled[i] = 0;
          continue;
        }
        const base = (0.55 + 0.25 * f.offset[i * 3 + 2]) * (1 - LOOK.textCalm + LOOK.textCalm * calmAt(l, x, y)) * LOOK.intensity;
        if (role === 1 && weight > 0) {
          const dx = gx - x;
          const dy = gy - y;
          f.settled[i] = weight >= 0.999 && dx * dx + dy * dy < 0.5 ? 1 : 0;
          if (f.settled[i]) continue;
          const alpha = Math.min(1, base * 0.8 + (0.95 * LOOK.intensity - base * 0.8) * weight);
          pushDisc(f.flyers, Math.min(5, Math.floor(alpha * 6)), x, y, speckRadius + (cellRadius(g, cell) - speckRadius) * weight);
          continue;
        }
        f.settled[i] = 0;
        const alpha = Math.min(1, base) * (role === 0 ? 1 - weight * weight : 1);
        if (alpha >= 0.04) pushPoint(f.specks, Math.min(9, Math.floor(alpha * 10)), x, y);
      }
    }

    const visible = g.visible;
    for (let idx = 0; idx < visible.length; idx++) {
      const owner = g.owner[idx];
      visible[idx] = g.depth[idx] === Infinity ? 0 : owner >= 0 && !reducedMotion ? f.settled[owner] : looseFade;
    }

    ctx.fillStyle = `rgba(${LOOK.accent},${(0.95 * LOOK.intensity).toFixed(3)})`;
    ctx.beginPath();
    for (let idx = 0; idx < visible.length; idx++) {
      if (visible[idx] <= 0) continue;
      const radius = cellRadius(g, idx) * visible[idx];
      if (radius < 0.5) continue;
      const x = g.gap / 2 + (idx % g.cols) * g.gap;
      const y = g.gap / 2 + Math.floor(idx / g.cols) * g.gap;
      ctx.moveTo(x + radius, y);
      ctx.arc(x, y, radius, 0, Math.PI * 2);
    }
    ctx.fill();

    f.flyers.data.forEach((data, level) => {
      const count = f.flyers.count[level];
      if (!count) return;
      ctx.fillStyle = `rgba(${LOOK.accent},${((level + 0.5) / 6).toFixed(3)})`;
      ctx.beginPath();
      for (let k = 0; k < count; k++) {
        const radius = Math.max(0.7, data[k * 3 + 2]);
        ctx.moveTo(data[k * 3] + radius, data[k * 3 + 1]);
        ctx.arc(data[k * 3], data[k * 3 + 1], radius, 0, Math.PI * 2);
      }
      ctx.fill();
    });

    ctx.globalCompositeOperation = "lighter";
    f.specks.data.forEach((data, level) => {
      const count = f.specks.count[level];
      if (!count) return;
      ctx.fillStyle = `rgba(${LOOK.accent},${(((level + 0.5) / 10) * 0.8).toFixed(3)})`;
      for (let k = 0; k < count; k++) {
        ctx.fillRect(data[k * 2] - speckRadius, data[k * 2 + 1] - speckRadius, FLOCK.particleSize, FLOCK.particleSize);
      }
    });
    ctx.globalCompositeOperation = "source-over";
  };

  const watchFrameRate = (interval: number) => {
    if (lite || interval > 250 || clock < ADAPT.warmup) return;
    averageInterval += (interval - averageInterval) * 0.05;
    samples += 1;
    if (samples < ADAPT.samples || averageInterval <= ADAPT.slowInterval) return;
    lite = true;
    if (layout) applyLayout(layout, Math.min(requestedRatio, ADAPT.liteRatio));
  };

  const tick = (now: number) => {
    frameId = requestAnimationFrame(tick);
    const interval = last ? now - last : 16;
    last = now;
    pendingStep += Math.min(64, interval);
    skipFrame = !skipFrame;
    if (lite && skipFrame) return;
    const step = Math.min(64, pendingStep);
    pendingStep = 0;
    clock += step;
    render(clock, step);
    watchFrameRate(interval);
  };

  const drawStill = () => {
    if (!running) render(clock, 0);
  };

  loadMascot(mascotUrl)
    .then((loaded) => {
      if (destroyed) return;
      mascot = loaded;
      projection = createProjection(loaded);
      drawStill();
    })
    .catch(() => undefined);

  const applyLayout = (next: HeroLayout, ratio: number) => {
    const previous = grid;
    layout = next;
    pixelRatio = ratio;
    canvas.width = Math.max(1, Math.round(next.width * ratio));
    canvas.height = Math.max(1, Math.round(next.height * ratio));
    grid = buildGrid(next, ratio, background);
    const keepFlock = flock !== null && flock.count === next.flockCount;
    if (!flock || !keepFlock) flock = createFlock(next.flockCount, next, clock);
    if (keepFlock && previous && previous.cols === grid.cols && previous.gap === grid.gap) {
      keepAssignments(flock, previous, grid);
    } else {
      flock.role.fill(-1);
      flock.settled.fill(0);
      cycleIndex = -1;
      reboundIndex = -1;
    }
    drawStill();
  };

  return {
    resize(next, ratio) {
      requestedRatio = ratio;
      applyLayout(next, lite ? Math.min(ratio, ADAPT.liteRatio) : ratio);
    },
    setPointer(next) {
      pointer = next;
    },
    start() {
      if (running || reducedMotion || destroyed) return;
      running = true;
      last = 0;
      pendingStep = 0;
      frameId = requestAnimationFrame(tick);
    },
    stop() {
      running = false;
      cancelAnimationFrame(frameId);
    },
    destroy() {
      destroyed = true;
      running = false;
      cancelAnimationFrame(frameId);
    },
  };
}
