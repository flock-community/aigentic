export type Rect = {
  left: number;
  top: number;
  right: number;
  bottom: number;
};

export type HeroLayout = {
  width: number;
  height: number;
  gap: number;
  birdX: number;
  footY: number;
  scale: number;
  calmX: number;
  calmY: number;
  calmRx: number;
  calmRy: number;
  flockScale: number;
  flockCount: number;
  swarmTop: number;
  swarmHeight: number;
};

export const STACKED_BELOW = 1200;

const DESIGN_AREA = 1440 * 810;
const DESIGN_SCALE = 217.5;
const MODEL_HEIGHT = 1.85;
const MODEL_WIDTH = 1.1;

const clamp = (value: number, min: number, max: number) => Math.min(max, Math.max(min, value));

function besideText(width: number, height: number, text: Rect) {
  const scale = DESIGN_SCALE * clamp(height / 900, 0.85, 1.15);
  const half = (MODEL_WIDTH * scale) / 2;
  return {
    scale,
    birdX: clamp(text.right + 0.6 * half, width / 2, width - half - 32),
    footY: height - Math.max(28, height * 0.045),
  };
}

function belowText(width: number, height: number, text: Rect) {
  const room = height - text.bottom - 88;
  return {
    scale: clamp(room, 150, 400) / MODEL_HEIGHT,
    birdX: width / 2,
    footY: height - 32,
  };
}

export function computeLayout(width: number, height: number, text: Rect): HeroLayout {
  const area = (width * height) / DESIGN_AREA;
  const stacked = width < STACKED_BELOW;
  const bird = stacked ? belowText(width, height, text) : besideText(width, height, text);
  const swarmTop = stacked ? Math.max(0, text.bottom - 140) : 0;
  return {
    width,
    height,
    gap: width < 768 ? 6 : 7,
    ...bird,
    calmX: (text.left + text.right) / 2,
    calmY: (text.top + text.bottom) / 2,
    calmRx: Math.max(80, (text.right - text.left) * 0.58),
    calmRy: Math.max(60, (text.bottom - text.top) * 0.55),
    flockScale: clamp(Math.sqrt(area), 0.55, 1.3),
    flockCount: Math.round(2200 * clamp(area, 0.45, 1.1)),
    swarmTop,
    swarmHeight: height - swarmTop,
  };
}
