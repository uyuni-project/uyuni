import * as React from "react";
import { memo } from "react";
import { VariableSizeList as List, ListChildComponentProps, areEqual } from "react-window";
import { useResizeDetector } from "react-resize-detector";

type Identifiable = { id: number | string };

type ListProps<T> = {
  renderRow: (item: T) => JSX.Element;
  rowHeight: (item: T) => number;
  items: T[];
};

const VirtualList = <T extends Identifiable>(props: ListProps<T>) => {
  const { width, height, ref } = useResizeDetector({
    refreshMode: "debounce",
    refreshRate: 100,
  });

  const Row = memo((rowProps: ListChildComponentProps<T[]>) => {
    const item = rowProps.data[rowProps.index];
    return (
      // react-window doesn't type `props.style` completely correctly
      <div style={rowProps.style as React.CSSProperties}>{props.renderRow(item)}</div>
    );
  }, areEqual);

  const itemSize = (index: number) => {
    const item = props.items[index];
    return props.rowHeight(item);
  };

  const itemKey = (index: number, data: T[]) => {
    const item = data[index];
    // Just going to hardcode this for now, make this a prop if you need it generic
    return item.id;
  };

  return (
    <div ref={ref} style={{ flex: "1 1 auto" }}>
      {typeof width !== "undefined" && typeof height !== "undefined" ? (
        <List
          width={width}
          height={height}
          itemData={props.items}
          itemCount={props.items.length}
          itemSize={itemSize}
          itemKey={itemKey}
          children={Row}
          // See https://react-window.vercel.app/#/api/FixedSizeList
          overscanCount={10}
        />
      ) : null}
    </div>
  );
};

export default VirtualList;
