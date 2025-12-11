import { useState } from "react";

export const useExpanded = <T>() => {
  const [expanded, setExpandedState] = useState<Set<T>>(new Set());

  return {
    toggle(item: T) {
      const newExpanded = new Set(expanded);
      if (expanded.has(item)) {
        newExpanded.delete(item);
      } else {
        newExpanded.add(item);
      }
      setExpandedState(newExpanded);
    },
    has(item: T) {
      return expanded.has(item);
    },
    set(items: T[]) {
      setExpandedState(new Set(items));
    },
    clear() {
      setExpandedState(new Set());
    },
    values: Array.from(expanded),
  };
};
