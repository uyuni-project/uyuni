import { useState } from "react";

export const useExpanded = <T>() => {
  const [expanded, setExpanded] = useState(new Set<T>());

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
  };
};
