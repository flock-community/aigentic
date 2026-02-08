import { AnimatePresence, motion } from 'motion/react';
import { useEffect, useState, useRef } from 'react';
import styles from './CyclingBlurText.module.css';

type CyclingBlurTextProps = {
  phrases: string[];
  prefix?: string;
  suffix?: string;
  className?: string;
};

const BLUR_IN_DURATION = 0.8;
const VISIBLE_PAUSE = 2500;
const BLUR_OUT_DURATION = 0.4;
const CYCLE_INTERVAL = BLUR_IN_DURATION * 1000 + VISIBLE_PAUSE + BLUR_OUT_DURATION * 1000;

const CyclingBlurText: React.FC<CyclingBlurTextProps> = ({
  phrases,
  prefix = '',
  suffix = '',
  className = '',
}) => {
  const [phraseIndex, setPhraseIndex] = useState(0);
  const timerRef = useRef<ReturnType<typeof setTimeout>>(undefined);

  useEffect(() => {
    timerRef.current = setTimeout(() => {
      setPhraseIndex((prev) => (prev + 1) % phrases.length);
    }, CYCLE_INTERVAL);

    return () => clearTimeout(timerRef.current);
  }, [phraseIndex, phrases.length]);

  const currentPhrase = phrases[phraseIndex];
  const words = currentPhrase.split(' ');

  return (
    <div className={className}>
      <div className={styles.prefixText}>{prefix}</div>
      <div className={styles.dynamicContainer}>
        <AnimatePresence mode="wait">
          <motion.span
            key={phraseIndex}
            className={styles.highlightedText}
            initial={{ filter: 'blur(10px)', opacity: 0, y: -50 }}
            animate={{ filter: 'blur(0px)', opacity: 1, y: 0 }}
            exit={{ opacity: 0, transition: { duration: BLUR_OUT_DURATION } }}
            transition={{
              duration: BLUR_IN_DURATION,
              ease: 'easeOut',
            }}
            style={{ display: 'inline-flex', flexWrap: 'wrap', justifyContent: 'center' }}
          >
            {words.map((word, index) => (
              <span key={index} style={{ color: '#101010' }}>
                {word}
                {index < words.length - 1 && '\u00A0'}
              </span>
            ))}
          </motion.span>
        </AnimatePresence>
      </div>
      <div className={styles.suffixText}>{suffix}</div>
    </div>
  );
};

export default CyclingBlurText;
