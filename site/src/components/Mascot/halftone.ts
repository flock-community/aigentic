import type { Mascot } from "./mascot";

export const SHADING = {
  dotScale: 0.75,
  contrast: 0.65,
  ambient: 0.28,
  rimLight: 0.3,
  tilt: (2 * Math.PI) / 180,
  perspective: 7,
  center: 0.95,
  footToCenter: 0.89,
};

export type HalftoneGrid = {
  gap: number;
  cols: number;
  rows: number;
  depth: Float32Array;
  shade: Float32Array;
};

export type Placement = { birdX: number; footY: number; scale: number };

export type Projection = { x: Float32Array; y: Float32Array; depth: Float32Array };

export const createProjection = (mascot: Mascot): Projection => ({
  x: new Float32Array(mascot.vertexCount),
  y: new Float32Array(mascot.vertexCount),
  depth: new Float32Array(mascot.vertexCount),
});

export const dotRadius = (gap: number, value: number) =>
  gap * 0.5 * SHADING.dotScale * Math.pow(Math.min(1, Math.max(0, value)), SHADING.contrast);

export function rasterize(mascot: Mascot, grid: HalftoneGrid, placement: Placement, yaw: number, light: number[], projection: Projection) {
  const { gap, cols, rows, depth: zb, shade } = grid;
  const midY = placement.footY - SHADING.footToCenter * placement.scale;
  const cy = Math.cos(yaw);
  const sy = Math.sin(yaw);
  const cp = Math.cos(SHADING.tilt);
  const sp = Math.sin(SHADING.tilt);
  const [lx, ld, lu] = light;
  const { positions: p, triangles, normals, albedo } = mascot;
  const { x: px, y: py, depth: pd } = projection;
  for (let i = 0; i < mascot.vertexCount; i++) {
    const xr = p[i * 3] * cy - p[i * 3 + 1] * sy;
    const yr = p[i * 3] * sy + p[i * 3 + 1] * cy;
    const zc = p[i * 3 + 2] - SHADING.center;
    const d = yr * cp - zc * sp;
    const up = yr * sp + zc * cp;
    const s = SHADING.perspective / (SHADING.perspective + d);
    px[i] = placement.birdX + xr * placement.scale * s;
    py[i] = midY - up * placement.scale * s;
    pd[i] = d;
  }
  const half = gap / 2;
  for (let t = 0; t < mascot.triangleCount; t++) {
    const nx = normals[t * 3];
    const ny = normals[t * 3 + 1];
    const nz = normals[t * 3 + 2];
    const nxr = nx * cy - ny * sy;
    const nyr = nx * sy + ny * cy;
    const nd = nyr * cp - nz * sp;
    if (nd >= 0) continue;
    const nu = nyr * sp + nz * cp;
    const a = triangles[t * 3];
    const b = triangles[t * 3 + 1];
    const c = triangles[t * 3 + 2];
    const ax = px[a];
    const ay = py[a];
    const bx = px[b];
    const by = py[b];
    const qx = px[c];
    const qy = py[c];
    const area = (bx - ax) * (qy - ay) - (by - ay) * (qx - ax);
    if (Math.abs(area) < 1e-6) continue;
    const i0 = Math.max(0, Math.ceil((Math.min(ax, bx, qx) - half) / gap));
    const i1 = Math.min(cols - 1, Math.floor((Math.max(ax, bx, qx) - half) / gap));
    const j0 = Math.max(0, Math.ceil((Math.min(ay, by, qy) - half) / gap));
    const j1 = Math.min(rows - 1, Math.floor((Math.max(ay, by, qy) - half) / gap));
    if (i0 > i1 || j0 > j1) continue;
    const lambert = Math.max(0, nxr * lx + nd * ld + nu * lu);
    const lit = SHADING.ambient + (1 - SHADING.ambient) * lambert;
    const rim = Math.pow(1 + nd, 3) * SHADING.rimLight;
    const inverse = 1 / area;
    for (let j = j0; j <= j1; j++) {
      const y = half + j * gap;
      for (let i = i0; i <= i1; i++) {
        const x = half + i * gap;
        const w0 = ((bx - x) * (qy - y) - (by - y) * (qx - x)) * inverse;
        const w1 = ((qx - x) * (ay - y) - (qy - y) * (ax - x)) * inverse;
        const w2 = 1 - w0 - w1;
        if (w0 < -1e-4 || w1 < -1e-4 || w2 < -1e-4) continue;
        const z = w0 * pd[a] + w1 * pd[b] + w2 * pd[c];
        const idx = j * cols + i;
        if (z < zb[idx]) {
          zb[idx] = z;
          shade[idx] = (w0 * albedo[a] + w1 * albedo[b] + w2 * albedo[c]) * lit + rim;
        }
      }
    }
  }
}
