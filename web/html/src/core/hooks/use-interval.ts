import { useEffect, useRef } from "react";

// Based on https://overreacted.io/making-setinterval-declarative-with-react-hooks/
function useInterval<T extends Function>(callback: T, delay: number) {
  const savedCallback = useRef<T>();

  // Remember the latest callback.
  useEffect(() => {
    savedCallback.current = callback;
  }, [callback]);

  // Set up the interval.
  useEffect(() => {
    function tick() {
      savedCallback.current?.();
    }
    if (delay !== null) {
      let id = setInterval(tick, delay);
      return () => clearInterval(id);
    }
  }, [delay]);
}

export default useInterval;
