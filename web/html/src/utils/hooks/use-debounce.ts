import { useCallback, useRef } from "react";

import { debounce } from "lodash";

/**
 * A hook that delays the execution of a function until a specified time has passed since the last time it was called.
 * This is useful for improving performance in scenarios like search inputs or API requests that shouldn't be
 * triggered too frequently.
 *
 * @template T - The type of the function being debounced.
 * @param {T} callback - The function to debounce.
 * @param {number} timeoutMs - The time (in milliseconds) to wait before calling the function.
 * @returns {(...args: Parameters<T>) => void} A debounced version of the function.
 *
 * @example
 * const debouncedSearch = useDebounce((query) => fetchResults(query), 300);
 * debouncedSearch("dummy text"); // Will only trigger if no new calls happen _within_ 300ms
 */
export function useDebounce<T extends (...args: any[]) => any>(
  callback: T,
  timeoutMs: number
): (...args: Parameters<T>) => void {
  const debouncedFn = useRef(
    debounce((...args: Parameters<T>) => callback(...args), timeoutMs, { leading: false, trailing: true })
  ).current;

  return debouncedFn;
}
