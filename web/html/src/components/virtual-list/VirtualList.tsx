import * as React from "react";
import { VariableSizeList as List, ListChildComponentProps } from "react-window";
import Size from "./Size";

const rowHeights = new Array(1000).fill(true).map(() => 25 + Math.round(Math.random() * 50));

// TODO: Make this into a prop instead
const getItemSize = (index) => rowHeights[index];

// TODO: Memo this
const Row = <T,>(props: ListChildComponentProps<T>) => {
  return (
    // TODO: key based on props.data
    // react-window doesn't type `props.style` completely correctly
    <div style={props.style as React.CSSProperties} key={props.index}>
      Row {props.index}
    </div>
  );
};

type ListProps<T> = {
  // TODO: Require a reasonable key generator function
  // TODO: Make ch9ldr
  // row: (props: ListChildComponentProps<T>) => JSX.Element;
};

const VirtualList = <T,>(props: ListProps<T>) => {
  return (
    <Size>
      {({ width, height }) => (
        <List width={width} height={height} itemCount={1000} itemSize={getItemSize}>
          {Row}
        </List>
      )}
    </Size>
  );
};

export default VirtualList;
