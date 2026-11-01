import { useEffect, useRef } from "react";
import clsx from "clsx";
import useBaseUrl from "@docusaurus/useBaseUrl";
import { createPoseClock, loadMascot, restartPoseClock, updatePose, type ActionName, type Mascot } from "./mascot";
import { createProjection, dotRadius, rasterize, type HalftoneGrid, type Projection } from "./halftone";
import styles from "./styles.module.css";

type MascotPortraitProps = {
  playlist: ActionName[];
  className?: string;
};

const ACCENT = "rgba(240,188,66,0.92)";
const LIGHT = [-0.42, -0.56, 0.71];
const BASE_YAW = -2.356;
const MODEL_HEIGHT = 2.05;

export default function MascotPortrait({ playlist, className }: MascotPortraitProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const mascotUrl = useBaseUrl("/img/hero-mascot.bin");
  const playlistKey = playlist.join(",");

  useEffect(() => {
    const canvas = canvasRef.current;
    const ctx = canvas?.getContext("2d");
    if (!canvas || !ctx) return undefined;
    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const actions = playlistKey.split(",") as ActionName[];
    const clock = createPoseClock();
    let mascot: Mascot | null = null;
    let projection: Projection | null = null;
    let grid: HalftoneGrid | null = null;
    let width = 0;
    let height = 0;
    let frameId = 0;
    let visible = false;
    let destroyed = false;
    let start = 0;

    const draw = (seconds: number) => {
      if (!mascot || !projection || !grid) return;
      const { gap, cols } = grid;
      grid.depth.fill(Infinity);
      updatePose(mascot, clock, seconds, reducedMotion ? ["idle"] : actions);
      const scale = height / MODEL_HEIGHT;
      const yaw = BASE_YAW + (reducedMotion ? 0 : Math.sin(seconds * 0.35) * 0.1);
      rasterize(mascot, grid, { birdX: width / 2, footY: height - height * 0.04, scale }, yaw, LIGHT, projection);
      ctx.clearRect(0, 0, width, height);
      ctx.fillStyle = ACCENT;
      ctx.beginPath();
      const { depth, shade } = grid;
      depth.forEach((z, idx) => {
        if (z === Infinity) return;
        const radius = dotRadius(gap, shade[idx] * 0.9);
        if (radius < 0.4) return;
        const x = gap / 2 + (idx % cols) * gap;
        const y = gap / 2 + Math.floor(idx / cols) * gap;
        ctx.moveTo(x + radius, y);
        ctx.arc(x, y, radius, 0, Math.PI * 2);
      });
      ctx.fill();
    };

    const tick = (now: number) => {
      frameId = requestAnimationFrame(tick);
      if (!start) start = now;
      draw((now - start) / 1000);
    };

    const play = () => {
      if (reducedMotion || destroyed || !mascot || frameId) return;
      start = 0;
      restartPoseClock(clock);
      frameId = requestAnimationFrame(tick);
    };

    const pause = () => {
      cancelAnimationFrame(frameId);
      frameId = 0;
    };

    const resize = () => {
      const box = canvas.getBoundingClientRect();
      if (!box.width || !box.height) return;
      const ratio = Math.min(window.devicePixelRatio || 1, 2);
      width = box.width;
      height = box.height;
      canvas.width = Math.round(width * ratio);
      canvas.height = Math.round(height * ratio);
      ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
      const gap = Math.max(4, Math.round(height / 52));
      const cols = Math.ceil(width / gap);
      const rows = Math.ceil(height / gap);
      grid = { gap, cols, rows, depth: new Float32Array(cols * rows), shade: new Float32Array(cols * rows) };
      if (!frameId) draw(0);
    };

    loadMascot(mascotUrl)
      .then((loaded) => {
        if (destroyed) return;
        mascot = loaded;
        projection = createProjection(loaded);
        resize();
        if (visible) play();
      })
      .catch(() => undefined);

    const resizeObserver = new ResizeObserver(resize);
    resizeObserver.observe(canvas);
    const visibilityObserver = new IntersectionObserver(
      ([entry]) => {
        visible = entry.isIntersecting;
        if (visible) play();
        else pause();
      },
      { threshold: 0.5 },
    );
    visibilityObserver.observe(canvas);

    return () => {
      destroyed = true;
      pause();
      resizeObserver.disconnect();
      visibilityObserver.disconnect();
    };
  }, [mascotUrl, playlistKey]);

  return <canvas ref={canvasRef} className={clsx(styles.portrait, className)} aria-hidden="true" />;
}
