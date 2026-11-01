const MAGIC = 0x314d4741;
const ROTATION_SCALE = 16384;
const TRANSLATION_SCALE = 4096;
const CROSSFADE_SECONDS = 0.6;
const ACTIONS = ["idle", "look_around", "tilt_curious", "nod_yes", "wave", "cheer", "bow_greet"] as const;

export type ActionName = (typeof ACTIONS)[number];

const CALM_PLAYLIST: ActionName[] = ["idle", "idle", "look_around", "idle", "tilt_curious", "idle", "nod_yes"];
const cache = new Map<string, Promise<Mascot>>();

const AMPLITUDE: Record<ActionName, number> = {
  idle: 0.85,
  look_around: 0.5,
  tilt_curious: 0.45,
  nod_yes: 0.4,
  wave: 0.9,
  cheer: 0.8,
  bow_greet: 0.85,
};

type Action = {
  frames: number;
  duration: number;
  matrices: Float32Array;
};

export type Mascot = {
  vertexCount: number;
  triangleCount: number;
  boneCount: number;
  fps: number;
  rest: Float32Array;
  triangles: Uint16Array;
  boneIndex: Uint8Array;
  boneWeight: Uint8Array;
  albedo: Float32Array;
  actions: Record<ActionName, Action>;
  positions: Float32Array;
  normals: Float32Array;
  pose: Float32Array;
  blend: Float32Array;
};

export type PoseClock = {
  index: number;
  start: number;
  previous: { name: ActionName; start: number; at: number } | null;
};

const align4 = (bytes: number) => Math.ceil(bytes / 4) * 4;
const smooth = (v: number) => v * v * (3 - 2 * v);
const playbackRate = (name: ActionName) => (name === "idle" ? 1 : 0.8);
const durationOf = (mascot: Mascot, name: ActionName) => mascot.actions[name].duration / playbackRate(name);

export const createPoseClock = (): PoseClock => ({ index: 0, start: -1, previous: null });

export function restartPoseClock(clock: PoseClock) {
  clock.index = 0;
  clock.start = -1;
  clock.previous = null;
}

async function fetchMascot(url: string): Promise<Mascot> {
  const response = await fetch(url);
  if (!response.ok) throw new Error(`Could not load the mascot (${response.status})`);
  return decodeMascot(await response.arrayBuffer());
}

export function loadMascot(url: string): Promise<Mascot> {
  const pending = cache.get(url) ?? fetchMascot(url);
  cache.set(url, pending);
  pending.catch(() => cache.delete(url));
  return pending;
}

function decodeMascot(buffer: ArrayBuffer): Mascot {
  const [magic, vertexCount, triangleCount, boneCount, fps, actionCount] = new Uint32Array(buffer, 0, 6);
  if (magic !== MAGIC || actionCount !== ACTIONS.length) throw new Error("Unexpected hero mascot format");
  const frameCounts = Array.from(new Uint32Array(buffer, 24, actionCount));
  let offset = 24 + actionCount * 4;
  const take = <T>(create: (start: number, length: number) => T, length: number, bytesPerItem: number): T => {
    const view = create(offset, length);
    offset += align4(length * bytesPerItem);
    return view;
  };
  const rest = take((s, n) => new Float32Array(buffer, s, n), vertexCount * 3, 4);
  const triangles = take((s, n) => new Uint16Array(buffer, s, n), triangleCount * 3, 2);
  const boneIndex = take((s, n) => new Uint8Array(buffer, s, n), vertexCount * 4, 1);
  const boneWeight = take((s, n) => new Uint8Array(buffer, s, n), vertexCount * 4, 1);
  const albedo = Float32Array.from(
    take((s, n) => new Uint8Array(buffer, s, n), vertexCount, 1),
    (v) => v / 255,
  );
  const actions = Object.fromEntries(
    ACTIONS.map((name, i) => {
      const quantized = take((s, n) => new Int16Array(buffer, s, n), frameCounts[i] * boneCount * 12, 2);
      const matrices = Float32Array.from(quantized, (v, j) => v / (j % 4 === 3 ? TRANSLATION_SCALE : ROTATION_SCALE));
      return [name, { frames: frameCounts[i], duration: (frameCounts[i] - 1) / fps, matrices }];
    }),
  ) as Record<ActionName, Action>;
  return {
    vertexCount,
    triangleCount,
    boneCount,
    fps,
    rest,
    triangles,
    boneIndex,
    boneWeight,
    albedo,
    actions,
    positions: new Float32Array(vertexCount * 3),
    normals: new Float32Array(triangleCount * 3),
    pose: new Float32Array(boneCount * 12),
    blend: new Float32Array(boneCount * 12),
  };
}

function sampleAction(mascot: Mascot, name: ActionName, seconds: number, out: Float32Array) {
  const action = mascot.actions[name];
  const size = mascot.boneCount * 12;
  const last = action.frames - 1;
  const position = (((seconds * mascot.fps) % last) + last) % last;
  const f0 = Math.floor(position);
  const u = position - f0;
  const a = f0 * size;
  const b = Math.min(last, f0 + 1) * size;
  const source = action.matrices;
  for (let i = 0; i < size; i++) out[i] = source[a + i] + (source[b + i] - source[a + i]) * u;
}

function dampen(mascot: Mascot, m: Float32Array, amount: number) {
  for (let bone = 0; bone < mascot.boneCount; bone++) {
    const o = bone * 12;
    let ax = 1 + amount * (m[o] - 1);
    let ay = amount * m[o + 4];
    let az = amount * m[o + 8];
    let bx = amount * m[o + 1];
    let by = 1 + amount * (m[o + 5] - 1);
    let bz = amount * m[o + 9];
    let length = Math.hypot(ax, ay, az) || 1;
    ax /= length;
    ay /= length;
    az /= length;
    const dot = ax * bx + ay * by + az * bz;
    bx -= dot * ax;
    by -= dot * ay;
    bz -= dot * az;
    length = Math.hypot(bx, by, bz) || 1;
    bx /= length;
    by /= length;
    bz /= length;
    m[o] = ax;
    m[o + 4] = ay;
    m[o + 8] = az;
    m[o + 1] = bx;
    m[o + 5] = by;
    m[o + 9] = bz;
    m[o + 2] = ay * bz - az * by;
    m[o + 6] = az * bx - ax * bz;
    m[o + 10] = ax * by - ay * bx;
    m[o + 3] *= amount;
    m[o + 7] *= amount;
    m[o + 11] *= amount;
  }
}

function samplePose(mascot: Mascot, name: ActionName, elapsed: number, out: Float32Array) {
  sampleAction(mascot, name, elapsed * playbackRate(name), out);
  dampen(mascot, out, AMPLITUDE[name]);
}

function skin(mascot: Mascot) {
  const { rest, positions, boneIndex, boneWeight, pose } = mascot;
  for (let i = 0; i < mascot.vertexCount; i++) {
    const x = rest[i * 3];
    const y = rest[i * 3 + 1];
    const z = rest[i * 3 + 2];
    let ox = 0;
    let oy = 0;
    let oz = 0;
    for (let k = 0; k < 4; k++) {
      const weight = boneWeight[i * 4 + k];
      if (!weight) continue;
      const o = boneIndex[i * 4 + k] * 12;
      const s = weight / 255;
      ox += s * (pose[o] * x + pose[o + 1] * y + pose[o + 2] * z + pose[o + 3]);
      oy += s * (pose[o + 4] * x + pose[o + 5] * y + pose[o + 6] * z + pose[o + 7]);
      oz += s * (pose[o + 8] * x + pose[o + 9] * y + pose[o + 10] * z + pose[o + 11]);
    }
    positions[i * 3] = ox;
    positions[i * 3 + 1] = oy;
    positions[i * 3 + 2] = oz;
  }
}

function computeNormals(mascot: Mascot) {
  const { positions: p, triangles, normals } = mascot;
  for (let t = 0; t < mascot.triangleCount; t++) {
    const a = triangles[t * 3] * 3;
    const b = triangles[t * 3 + 1] * 3;
    const c = triangles[t * 3 + 2] * 3;
    const ux = p[b] - p[a];
    const uy = p[b + 1] - p[a + 1];
    const uz = p[b + 2] - p[a + 2];
    const vx = p[c] - p[a];
    const vy = p[c + 1] - p[a + 1];
    const vz = p[c + 2] - p[a + 2];
    const nx = uy * vz - uz * vy;
    const ny = uz * vx - ux * vz;
    const nz = ux * vy - uy * vx;
    const length = Math.hypot(nx, ny, nz) || 1;
    normals[t * 3] = nx / length;
    normals[t * 3 + 1] = ny / length;
    normals[t * 3 + 2] = nz / length;
  }
}

export function updatePose(mascot: Mascot, clock: PoseClock, seconds: number, playlist: ActionName[] = CALM_PLAYLIST) {
  if (clock.start < 0) clock.start = seconds;
  let name = playlist[clock.index % playlist.length];
  if (seconds - clock.start >= durationOf(mascot, name)) {
    clock.previous = { name, start: clock.start, at: seconds };
    clock.index += 1;
    clock.start = seconds;
    name = playlist[clock.index % playlist.length];
  }
  samplePose(mascot, name, seconds - clock.start, mascot.pose);
  const previous = clock.previous;
  if (previous && seconds - previous.at < CROSSFADE_SECONDS) {
    const elapsed = Math.min(seconds - previous.start, durationOf(mascot, previous.name) - 0.001);
    samplePose(mascot, previous.name, elapsed, mascot.blend);
    const u = smooth((seconds - previous.at) / CROSSFADE_SECONDS);
    for (let i = 0; i < mascot.pose.length; i++) {
      mascot.pose[i] = mascot.blend[i] + (mascot.pose[i] - mascot.blend[i]) * u;
    }
  }
  skin(mascot);
  computeNormals(mascot);
}
