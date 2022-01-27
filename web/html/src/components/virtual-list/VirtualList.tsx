import * as React from "react";
import { forwardRef, useRef, useEffect, useImperativeHandle } from "react";
import { VariableSizeList as List, ListChildComponentProps } from "react-window";
import { useResizeDetector } from "react-resize-detector";

export type Identifiable = {
  id: number | string;
};

type ListProps<T> = {
  renderRow: (item: T, index: number) => JSX.Element;
  rowHeight: (item: T) => number;
  estimatedRowHeight?: number;
  items: T[];
};

// const VirtualList = <T extends Identifiable>(props: ListProps<T>) => {
// TODO: Make generic, fix types
const VirtualList = forwardRef((props: ListProps<any>, todoRef) => {
  // The viewport of the virtual list needs to be strictly sized so it can precompute layouts
  const {
    width,
    height,
    ref: wrapperRef,
  } = useResizeDetector({
    refreshMode: "debounce",
    refreshRate: 100,
  });

  // When the items change, update layouts
  const listRef = useRef<List>(null);
  useEffect(() => {
    listRef.current?.resetAfterIndex(0);
  }, [props.items]);

  useImperativeHandle(todoRef, () => ({
    resetAfterIndex(index: number) {
      listRef.current?.resetAfterIndex(index);
    },
  }));

  const Row = (rowProps: ListChildComponentProps<Identifiable[]>) => {
    const item = rowProps.data[rowProps.index];
    const style: React.CSSProperties = {
      whiteSpace: "nowrap",
      maxWidth: "100%",
      overflow: "hidden",
      // react-window has outdated type declarations for React.CSSProperties, the values are correct for our use case
      ...(rowProps.style as Partial<React.CSSProperties>),
    };
    return <div style={style}>{props.renderRow(item, rowProps.index)}</div>;
  };

  const itemSize = (index: number) => {
    const item = props.items[index];
    return props.rowHeight(item);
  };

  const itemKey = (index: number, data: Identifiable[]) => {
    const item = data[index];
    // Just going to hardcode this for now, make this a prop if you need it generic
    return item.id;
  };

  return (
    <div ref={wrapperRef} style={{ flex: "1 1 auto" }}>
      {typeof width !== "undefined" && typeof height !== "undefined" ? (
        <List
          ref={listRef}
          width={width}
          height={height}
          itemData={props.items}
          itemCount={props.items.length}
          itemSize={itemSize}
          estimatedItemSize={props.estimatedRowHeight}
          itemKey={itemKey}
          children={Row}
          // How many items to prerender outside of the viewport, see https://react-window.vercel.app/#/api/FixedSizeList
          overscanCount={20}
        />
      ) : null}
    </div>
  );
});

export default VirtualList;
