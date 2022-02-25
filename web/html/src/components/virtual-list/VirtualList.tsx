import * as React from "react";

import { Virtuoso } from "react-virtuoso";

type ListProps<T> = {
  renderItem: (item: T) => JSX.Element;
  items: T[];
  itemKey: (item: T) => string | number;
  defaultItemHeight?: number;
};

const VirtualList = <T,>(props: ListProps<T>) => {
  const itemContent = (index: number, item: T) => {
    return props.renderItem(item);
  };

  const computeItemKey = (index: number, item: T) => props.itemKey(item);

  return (
    <div style={{ flex: "1 1 auto" }}>
      <Virtuoso
        data={props.items}
        itemContent={itemContent}
        computeItemKey={computeItemKey}
        overscan={20}
        defaultItemHeight={props.defaultItemHeight}
      />
    </div>
  );
};

export default VirtualList;
