import * as React from "react";
import { forwardRef, useRef, useEffect, useImperativeHandle } from "react";
import { VariableSizeList as List, ListChildComponentProps } from "react-window";
import { Virtuoso } from "react-virtuoso";
import { useResizeDetector } from "react-resize-detector";

export type Identifiable = {
  id: number | string;
};

type ListProps<T> = {
  renderItem: (item: T, index: number) => JSX.Element;
  items: T[];
};

// TODO: See https://github.com/DevExpress/testcafe/issues/4857#issuecomment-598775956 for the error in the console
const VirtualList = <T extends Identifiable>(props: ListProps<T>) => {
  const itemContent = (index: number, item: T) => {
    return props.renderItem(item, index);
  };

  return (
    <div style={{ flex: "1 1 auto" }}>
      <Virtuoso data={props.items} itemContent={itemContent} overscan={20} />
    </div>
  );
};

export default VirtualList;
