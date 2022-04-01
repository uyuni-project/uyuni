import { useEffect, useRef } from "react";

// Based on https://overreacted.io/making-setinterval-declarative-with-react-hooks/
/**
 * `useEffect()` composed with `setInterval()` with dependency tracking and cleanup
 */
export function useIntervalEffect(
  effect: React.EffectCallback,
  interval: number | null,
  dependencies: React.DependencyList
) {
  const savedEffect = useRef<React.EffectCallback>();

  // Remember the latest callback.
  useEffect(() => {
    savedEffect.current = effect;
  }, [effect]);

  // Set up the interval.
  useEffect(() => {
    let cleanup: ReturnType<React.EffectCallback> | undefined;
    function tick() {
      // The effect may yield a cleanup function similar to regular hooks
      cleanup = savedEffect.current?.();
    }

    if (interval !== null) {
      let id = setInterval(tick, interval);
      return () => {
        clearInterval(id);
        if (typeof cleanup === "function") {
          cleanup();
        }
      };
    }
  }, dependencies);
}
