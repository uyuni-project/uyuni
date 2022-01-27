import * as React from "react";
import { VariableSizeTree as Tree } from "react-vtree";
import { useResizeDetector } from "react-resize-detector";

export type Identifiable = {
  id: string;
};

// TODO: Update types
type NodeProps<T> = {
  data: T;
  isOpen: boolean;
  style: React.CSSProperties;
  setOpen: (isOpen: boolean) => void;
};

type TreeProps<T> = {
  treeWalker?: () => Generator<T>;
  renderRow: (props: NodeProps<T>) => JSX.Element;
  estimatedRowHeight: number;
};

const VirtualTree = <T extends Identifiable>(props: TreeProps<T>) => {
  // The viewport of the virtual list needs to be strictly sized so it can precompute layouts
  const { width, height, ref } = useResizeDetector({
    refreshMode: "debounce",
    refreshRate: 100,
  });

  const Node = (nodeProps: NodeProps<T>) => {
    return <div style={nodeProps.style}>{props.renderRow(nodeProps)}</div>;
  };

  return (
    <div ref={ref} style={{ flex: "1 1 auto" }}>
      {typeof width !== "undefined" && typeof height !== "undefined" && typeof props.treeWalker !== "undefined" ? (
        // TODO: Types
        <Tree treeWalker={props.treeWalker as any} width={width} height={height}>
          {Node as any}
        </Tree>
      ) : null}
    </div>
  );
};

export default VirtualTree;
