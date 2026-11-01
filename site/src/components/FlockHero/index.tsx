import { useEffect, useRef, useState, type RefObject } from "react";
import useBaseUrl from "@docusaurus/useBaseUrl";
import useHydratedReducedMotion from "@site/src/hooks/useHydratedReducedMotion";
import { createFlockHero, type FlockHero as FlockHeroEngine } from "./engine";
import { computeLayout, type Rect } from "./layout";
import styles from "./styles.module.css";

type FlockHeroProps = {
  contentRef: RefObject<HTMLElement | null>;
};

function textBounds(content: HTMLElement, origin: DOMRect): Rect {
  const range = document.createRange();
  const walker = document.createTreeWalker(content, NodeFilter.SHOW_TEXT);
  const bounds = { left: Infinity, top: Infinity, right: -Infinity, bottom: -Infinity };
  for (let node = walker.nextNode(); node; node = walker.nextNode()) {
    if (!node.textContent?.trim()) continue;
    range.selectNodeContents(node);
    const rect = range.getBoundingClientRect();
    if (!rect.width) continue;
    bounds.left = Math.min(bounds.left, rect.left);
    bounds.top = Math.min(bounds.top, rect.top);
    bounds.right = Math.max(bounds.right, rect.right);
    bounds.bottom = Math.max(bounds.bottom, rect.bottom);
  }
  const box = bounds.left === Infinity ? content.getBoundingClientRect() : bounds;
  return {
    left: box.left - origin.left,
    top: box.top - origin.top,
    right: box.right - origin.left,
    bottom: box.bottom - origin.top,
  };
}

export default function FlockHero({ contentRef }: FlockHeroProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const engineRef = useRef<FlockHeroEngine | null>(null);
  const visibleRef = useRef(false);
  const pausedRef = useRef(false);
  const [paused, setPaused] = useState(false);
  const reduceMotion = useHydratedReducedMotion();
  const mascotUrl = useBaseUrl("/img/hero-mascot.bin");

  useEffect(() => {
    const canvas = canvasRef.current;
    const hero = canvas?.parentElement;
    const content = contentRef.current;
    if (!canvas || !hero || !content) return undefined;

    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const flock = createFlockHero(canvas, mascotUrl, reducedMotion);
    engineRef.current = flock;
    const relayout = () => {
      const box = hero.getBoundingClientRect();
      if (!box.width || !box.height) return;
      flock.resize(computeLayout(box.width, box.height, textBounds(content, box)), Math.min(window.devicePixelRatio || 1, 2));
    };
    const onPointerMove = (event: PointerEvent) => {
      if (event.pointerType !== "mouse") return;
      const box = hero.getBoundingClientRect();
      flock.setPointer({ x: event.clientX - box.left, y: event.clientY - box.top });
    };
    const onPointerLeave = () => flock.setPointer(null);

    const resizeObserver = new ResizeObserver(relayout);
    resizeObserver.observe(hero);
    const visibilityObserver = new IntersectionObserver(([entry]) => {
      visibleRef.current = entry.isIntersecting;
      if (entry.isIntersecting && !pausedRef.current) flock.start();
      else flock.stop();
    });
    visibilityObserver.observe(hero);
    document.fonts?.ready.then(relayout);
    hero.addEventListener("pointermove", onPointerMove);
    hero.addEventListener("pointerleave", onPointerLeave);

    return () => {
      resizeObserver.disconnect();
      visibilityObserver.disconnect();
      hero.removeEventListener("pointermove", onPointerMove);
      hero.removeEventListener("pointerleave", onPointerLeave);
      flock.destroy();
      engineRef.current = null;
    };
  }, [contentRef, mascotUrl]);

  const togglePaused = () => {
    const next = !pausedRef.current;
    pausedRef.current = next;
    setPaused(next);
    if (next) engineRef.current?.stop();
    else if (visibleRef.current) engineRef.current?.start();
  };

  return (
    <>
      <canvas ref={canvasRef} className={styles.canvas} aria-hidden="true" />
      {!reduceMotion && (
        <button
          type="button"
          className={styles.toggle}
          onClick={togglePaused}
          aria-label={paused ? "Play background animation" : "Pause background animation"}
        >
          {paused ? (
            <svg viewBox="0 0 16 16" aria-hidden="true">
              <path d="M5 3.5v9a.5.5 0 0 0 .77.42l7-4.5a.5.5 0 0 0 0-.84l-7-4.5A.5.5 0 0 0 5 3.5Z" />
            </svg>
          ) : (
            <svg viewBox="0 0 16 16" aria-hidden="true">
              <rect x="4" y="3" width="3" height="10" rx="1" />
              <rect x="9" y="3" width="3" height="10" rx="1" />
            </svg>
          )}
        </button>
      )}
    </>
  );
}
