import * as React from "react";
import { useRef, useState, useEffect } from "react";

type Props = {
  children: ({ width, height }) => JSX.Element;
};

/**
 * TODO: This doesn't account for stuff like parent resizes and should be replaced by something a-la
 *  - https://github.com/maslianok/react-resize-detector
 *  - https://github.com/crimx/react-resize-reporter
 *  - etc
 */
const Size = (props: Props) => {
  const container = useRef<HTMLDivElement | null>(null);
  const [size, setSize] = useState<{ width: number; height: number } | null>(null);

  useEffect(() => {
    if (!container.current) {
      return;
    }
    const width = container.current.clientWidth;
    const height = container.current.clientHeight;
    if (size?.width === width || size?.height === height) {
      return;
    }
    setSize({ width, height });
  });

  // TODO: Move to styles
  return (
    <div ref={container} style={{ flex: "1 1 auto" }}>
      {size ? props.children(size) : null}
    </div>
  );
};

export default Size;
