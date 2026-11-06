import type { ReactNode } from "react";
import useBaseUrl from "@docusaurus/useBaseUrl";
import styles from "./styles.module.css";

type ScreenshotProps = {
  src: string;
  alt: string;
  width: number;
  height: number;
  caption?: ReactNode;
  narrow?: boolean;
};

/* Stills are captured at 2x from the platform's own components with demo data; width and height are the CSS size. */
export function Screenshot({ src, alt, width, height, caption, narrow }: ScreenshotProps) {
  const url = useBaseUrl(src);
  return (
    <figure className={styles.figure} style={narrow ? { maxWidth: width } : undefined}>
      <a className={styles.zoom} href={url} target="_blank" rel="noopener" title="Open full size">
        <img
          className={styles.media}
          src={url}
          alt={alt}
          width={width}
          height={height}
          loading="lazy"
          decoding="async"
        />
      </a>
      {caption && <figcaption className={styles.caption}>{caption}</figcaption>}
    </figure>
  );
}

type VideoProps = {
  src: string;
  poster: string;
  width: number;
  height: number;
  label: string;
  caption?: ReactNode;
};

export function PlatformVideo({ src, poster, width, height, label, caption }: VideoProps) {
  return (
    <figure className={styles.figure}>
      <video
        className={styles.media}
        controls
        muted
        playsInline
        preload="none"
        poster={useBaseUrl(poster)}
        width={width}
        height={height}
        aria-label={label}
      >
        <source src={useBaseUrl(src)} type="video/mp4" />
      </video>
      {caption && <figcaption className={styles.caption}>{caption}</figcaption>}
    </figure>
  );
}
