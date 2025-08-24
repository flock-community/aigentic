import React, { useState, useEffect } from 'react';
import clsx from 'clsx';
import styles from './TypeCursor.module.css';

interface TypeCursorProps {
  phrases?: string[];
  typingSpeed?: number;
  deletingSpeed?: number;
  delayAfterPhrase?: number;
  delayBeforeDelete?: number;
  prefix?: string;
  suffix?: string;
  cursorBlinkSpeed?: number;
  className?: string;
}

const TypeCursor: React.FC<TypeCursorProps> = ({
  phrases = ['production-ready', 'reliable', 'LLM-agnostic'],
  typingSpeed = 120,
  deletingSpeed = 100,
  delayAfterPhrase = 1500,
  delayBeforeDelete = 1500,
  prefix = 'Aigentic helps you build',
  suffix = 'AI agents faster',
  cursorBlinkSpeed = 450,
  className,
}) => {
  const [text, setText] = useState('');
  const [phraseIndex, setPhraseIndex] = useState(0);
  const [isDeleting, setIsDeleting] = useState(false);
  const [cursorVisible, setCursorVisible] = useState(true);
  const [isPaused, setIsPaused] = useState(false);

  // Cursor blinking effect
  useEffect(() => {
    const cursorInterval = setInterval(() => {
      setCursorVisible(prev => !prev);
    }, cursorBlinkSpeed);

    return () => clearInterval(cursorInterval);
  }, [cursorBlinkSpeed]);

  // Helper function to get variable typing speed
  const getVariableSpeed = (baseSpeed: number, char: string) => {
    // Check if user prefers reduced motion
    const prefersReducedMotion = typeof window !== 'undefined' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (prefersReducedMotion) {
      return baseSpeed;
    }

    // Add slight variation for human feel
    const variation = Math.random() * 40 - 20; // ±20ms variation

    // Slower for punctuation and spaces
    if ([' ', '-', '.', ','].includes(char)) {
      return baseSpeed + 30 + variation;
    }

    return baseSpeed + variation;
  };

  useEffect(() => {
    if (isPaused) return;

    const currentPhrase = phrases[phraseIndex];
    let timeout: NodeJS.Timeout;

    // If we're deleting
    if (isDeleting) {
      if (text.length === 0) {
        // When fully deleted, move to the next phrase
        setIsDeleting(false);
        setPhraseIndex((prev) => (prev + 1) % phrases.length);
        timeout = setTimeout(() => {}, delayAfterPhrase);
      } else {
        // Delete the next character
        const speed = deletingSpeed
        timeout = setTimeout(() => {
          setText(text.substring(0, text.length - 1));
        }, speed);
      }
    }
    // If we're typing
    else {
      if (text.length < currentPhrase.length) {
        // Type the next character
        const speed = typingSpeed;
        timeout = setTimeout(() => {
          setText(currentPhrase.substring(0, text.length + 1));
        }, speed);
      } else {
        // Fully typed, wait and then start deleting
        timeout = setTimeout(() => {
          setIsDeleting(true);
        }, delayBeforeDelete);
      }
    }

    return () => clearTimeout(timeout);
  }, [
    text,
    isDeleting,
    phraseIndex,
    phrases,
    typingSpeed,
    deletingSpeed,
    delayAfterPhrase,
    delayBeforeDelete,
    isPaused
  ]);

  return (
    <div
      className={clsx(styles.typeCursor, className)}
      onMouseEnter={() => setIsPaused(true)}
      onMouseLeave={() => setIsPaused(false)}
    >
      <div className={styles.textContainer}>
        <div className={styles.prefixText}>{prefix}</div>
        <div className={styles.dynamicContainer}>
          <span className={styles.highlightedText}>
            {text}
            <span
              className={clsx(styles.cursor, {
                [styles.cursorVisible]: cursorVisible
              })}
            >
              |
            </span>
          </span>
        </div>
        <div className={styles.suffixText}>{suffix}</div>
      </div>
    </div>
  );
};

export default TypeCursor;
