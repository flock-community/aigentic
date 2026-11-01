import useIsBrowser from "@docusaurus/useIsBrowser";
import { useReducedMotion } from "motion/react";

export default function useHydratedReducedMotion(): boolean {
  const isBrowser = useIsBrowser();
  const reduceMotion = useReducedMotion();
  return isBrowser && reduceMotion === true;
}
