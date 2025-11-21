import type { KeyboardEvent } from "react";

/**
 * @deprecated Do NOT use this for new code, this only exists to add missing keyboard handlers to legacy code.
 * If you want to use this function, use a `<button>` element instead.
 */
export const DEPRECATED_onClick = (callback?: (...args: any[]) => any) => ({
  onClick: callback,
  onKeyDown: (event: KeyboardEvent<HTMLElement>) => {
    if (event.key === "Enter" && callback) {
      event.preventDefault();
      callback();
    }
  },
  role: "button",
});
