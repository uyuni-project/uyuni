import * as React from "react";
import { useEffect, useRef } from "react";

type Props = {
  onFocusOut: () => void;
  children?: React.ReactNode;
};

export const FocusGroup = (props: Props) => {
  const containerRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    const container = containerRef.current;
    if (!container) {
      return;
    }

    // See https://stackoverflow.com/a/38317768/1470607
    let timerId: number | undefined = undefined;
    const onFocusIn = () => {
      window.clearTimeout(timerId);
    };
    const onFocusOut = () => {
      window.clearTimeout(timerId);
      timerId = window.setTimeout(() => props.onFocusOut?.(), 0);
    };

    container.addEventListener("focusin", onFocusIn);
    container.addEventListener("focusout", onFocusOut);
    return () => {
      container.removeEventListener("focusin", onFocusIn);
      container.removeEventListener("focusout", onFocusOut);
    };
  }, [containerRef.current]);

  return <div ref={containerRef}>{props.children}</div>;
};
