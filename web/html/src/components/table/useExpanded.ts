import { useState } from "react";

export const useExpanded = <T>() => {
  const [expanded, setExpanded] = useState<Set<T>>(new Set());

  return {
    toggle(item: T) {
      const newExpanded = new Set(expanded);
      if (expanded.has(item)) {
        newExpanded.delete(item);
      } else {
        newExpanded.add(item);
      }
      setExpanded(newExpanded);
    },
    has(item: T) {
      return expanded.has(item);
    },
    set(items: T[]) {
      setExpanded(new Set(items));
    },
    clear() {
      setExpanded(new Set());
    },
    values: Array.from(expanded),
  };
};
