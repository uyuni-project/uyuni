import { useEffect, useRef } from "react";
import _throttle from "lodash/throttle";

/**
 * Get the value of a variable from the previous render of a functional component, initially returns undefined.
 * Can be used for variables, props, state, or anything really.
 */
export function usePrevious<T>(value: T) {
  const ref = useRef<T>();
  useEffect(() => {
    ref.current = value;
  });
  return ref.current;
}

/**
 * Get the closest scrollable parent of an element. The yielded element might not have a scroll bar yet, but would be the scrollable parent if there was one.
 * See https://stackoverflow.com/a/42543908/1470607
 */
function getScrollParent(element: HTMLElement, includeHidden: boolean) {
  var style = getComputedStyle(element);
  var excludeStaticParent = style.position === "absolute";
  var overflowRegex = includeHidden ? /(auto|scroll|hidden)/ : /(auto|scroll)/;

  if (style.position === "fixed") return document.body;
  for (var parent: HTMLElement | null = element; (parent = parent.parentElement); ) {
    style = getComputedStyle(parent);
    if (excludeStaticParent && style.position === "static") {
      continue;
    }
    if (overflowRegex.test(style.overflow + style.overflowY + style.overflowX)) return parent;
  }

  return document.body;
}

/**
 * Listen to scroll events on the closest scrollable parent element, throttled to once per 100ms.
 */
export function useOnClosestScroll(ref: React.RefObject<HTMLElement>, onScroll: (event: Event) => void) {
  useEffect(() => {
    if (!ref.current) return;

    // TODO: On every render, if there's no overflow, try loading more until there's nothing left

    const throttledOnScroll = _throttle(onScroll, 100);
    const scrollParent = getScrollParent(ref.current, false);
    scrollParent.addEventListener("scroll", throttledOnScroll);
    return () => {
      scrollParent.removeEventListener("scroll", throttledOnScroll);
    };
  }, [ref.current]);
}
