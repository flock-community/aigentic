import { useEffect, useRef, type RefObject } from "react";
import useBaseUrl from "@docusaurus/useBaseUrl";
import { createFlockHero } from "./engine";
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
  const mascotUrl = useBaseUrl("/img/hero-mascot.bin");

  useEffect(() => {
    const canvas = canvasRef.current;
    const hero = canvas?.parentElement;
    const content = contentRef.current;
    if (!canvas || !hero || !content) return undefined;

    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const flock = createFlockHero(canvas, mascotUrl, reducedMotion);
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
    const visibilityObserver = new IntersectionObserver(([entry]) => (entry.isIntersecting ? flock.start() : flock.stop()));
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
    };
  }, [contentRef, mascotUrl]);

  return <canvas ref={canvasRef} className={styles.canvas} aria-hidden="true" />;
}
