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
const PHRASE_STYLE = { display: 'inline-flex', flexWrap: 'wrap', justifyContent: 'center' } as const;

const PhraseWords = ({ phrase }: { phrase: string }) => {
  const words = phrase.split(' ');
  return (
    <>
      {words.map((word, index) => (
        <span key={index} style={{ color: '#101010' }}>
          {word}
          {index < words.length - 1 && '\u00A0'}
        </span>
      ))}
    </>
  );
};

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

  return (
    <span className={`${styles.root} ${className}`}>
      <span className={styles.prefixText}>{prefix}</span>
      <span className={styles.dynamicContainer}>
        {phrases.map((phrase) => (
          <span key={phrase} className={`${styles.highlightedText} ${styles.sizer}`} style={PHRASE_STYLE} aria-hidden="true">
            <PhraseWords phrase={phrase} />
          </span>
        ))}
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
            style={PHRASE_STYLE}
          >
            <PhraseWords phrase={currentPhrase} />
          </motion.span>
        </AnimatePresence>
      </span>
      <span className={styles.suffixText}>{suffix}</span>
    </span>
  );
};

export default CyclingBlurText;
